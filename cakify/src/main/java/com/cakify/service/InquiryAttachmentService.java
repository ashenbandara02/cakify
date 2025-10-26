package com.cakify.service;

import com.cakify.dto.InquiryAttachmentResponse;
import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryAttachment;
import com.cakify.repository.InquiryAttachmentRepository;
import com.cakify.repository.InquiryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryAttachmentService {
    
    private final InquiryAttachmentRepository attachmentRepository;
    private final InquiryRepository inquiryRepository;
    
    @Value("${inquiry.attachment.upload-dir:uploads/inquiry-attachments}")
    private String uploadDir;
    
    @Value("${inquiry.attachment.max-file-size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${inquiry.attachment.max-files-per-inquiry:5}")
    private int maxFilesPerInquiry;
    
    // Allowed file types for inquiry attachments
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );
    
    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif",
            ".pdf",
            ".doc", ".docx",
            ".xls", ".xlsx",
            ".txt"
    );
    
    private Path fileStorageLocation;
    
    /**
     * Initialize storage directory on application startup
     */
    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("✅ Inquiry attachment directory created at: " + this.fileStorageLocation);
        } catch (IOException ex) {
            System.err.println("❌ Could not create inquiry attachment directory: " + ex.getMessage());
            throw new RuntimeException("Could not create inquiry attachment directory!", ex);
        }
    }
    
    /**
     * Upload attachment for an inquiry
     * @param inquiryId The inquiry ID
     * @param file The file to upload
     * @return InquiryAttachmentResponse DTO
     */
    public InquiryAttachmentResponse uploadAttachment(Long inquiryId, MultipartFile file) {
        // Validate inquiry exists
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found with id: " + inquiryId));
        
        // Check max files limit
        long currentFileCount = attachmentRepository.countByInquiryId(inquiryId);
        if (currentFileCount >= maxFilesPerInquiry) {
            throw new IllegalArgumentException(
                String.format("Maximum number of attachments (%d) reached for this inquiry", maxFilesPerInquiry)
            );
        }
        
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename(), inquiryId);
        
        // Create inquiry-specific subdirectory
        Path inquiryDir = fileStorageLocation.resolve("inquiry_" + inquiryId);
        try {
            Files.createDirectories(inquiryDir);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create inquiry directory", ex);
        }
        
        // Save file to disk
        String savedFilePath = saveFile(file, inquiryDir, uniqueFilename);
        
        // Create attachment entity
        InquiryAttachment attachment = new InquiryAttachment();
        attachment.setInquiry(inquiry);
        attachment.setFileName(uniqueFilename);
        attachment.setFilePath(savedFilePath);
        attachment.setFileUrl(savedFilePath); // Set fileUrl as well for database NOT NULL constraint
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        
        // Save to database
        InquiryAttachment savedAttachment = attachmentRepository.save(attachment);
        
        System.out.println("✅ Attachment uploaded: " + uniqueFilename + " for inquiry: " + inquiryId);
        
        return InquiryAttachmentResponse.fromEntity(savedAttachment);
    }
    
    /**
     * Get all attachments for an inquiry
     * @param inquiryId The inquiry ID
     * @return List of attachment response DTOs
     */
    public List<InquiryAttachmentResponse> getAttachmentsByInquiryId(Long inquiryId) {
        List<InquiryAttachment> attachments = attachmentRepository.findByInquiryIdOrderByUploadedAtDesc(inquiryId);
        return attachments.stream()
                .map(InquiryAttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a single attachment by ID
     * @param attachmentId The attachment ID
     * @return Optional attachment response DTO
     */
    public Optional<InquiryAttachmentResponse> getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(InquiryAttachmentResponse::fromEntity);
    }
    
    /**
     * Get image attachments for an inquiry
     * @param inquiryId The inquiry ID
     * @return List of image attachments
     */
    public List<InquiryAttachmentResponse> getImageAttachments(Long inquiryId) {
        List<InquiryAttachment> attachments = attachmentRepository.findImageAttachmentsByInquiryId(inquiryId);
        return attachments.stream()
                .map(InquiryAttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get document attachments for an inquiry
     * @param inquiryId The inquiry ID
     * @return List of document attachments
     */
    public List<InquiryAttachmentResponse> getDocumentAttachments(Long inquiryId) {
        List<InquiryAttachment> attachments = attachmentRepository.findDocumentAttachmentsByInquiryId(inquiryId);
        return attachments.stream()
                .map(InquiryAttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete an attachment
     * @param attachmentId The attachment ID
     * @return true if deleted, false if not found
     */
    public boolean deleteAttachment(Long attachmentId) {
        Optional<InquiryAttachment> attachmentOpt = attachmentRepository.findById(attachmentId);
        
        if (attachmentOpt.isEmpty()) {
            return false;
        }
        
        InquiryAttachment attachment = attachmentOpt.get();
        
        // Delete physical file
        deletePhysicalFile(attachment.getFilePath());
        
        // Delete from database
        attachmentRepository.delete(attachment);
        
        System.out.println("✅ Attachment deleted: " + attachment.getFileName());
        
        return true;
    }
    
    /**
     * Delete all attachments for an inquiry
     * @param inquiryId The inquiry ID
     * @return Number of attachments deleted
     */
    public int deleteAllAttachmentsByInquiryId(Long inquiryId) {
        List<InquiryAttachment> attachments = attachmentRepository.findByInquiryIdOrderByUploadedAtDesc(inquiryId);
        
        // Delete physical files
        attachments.forEach(attachment -> deletePhysicalFile(attachment.getFilePath()));
        
        // Delete from database
        attachmentRepository.deleteByInquiryId(inquiryId);
        
        System.out.println("✅ Deleted " + attachments.size() + " attachments for inquiry: " + inquiryId);
        
        return attachments.size();
    }
    
    /**
     * Load attachment as resource for download
     * @param fileName The file name
     * @return Resource for download
     */
    public Resource loadAttachmentAsResource(String fileName) {
        try {
            // Search in all inquiry subdirectories
            Optional<InquiryAttachment> attachmentOpt = attachmentRepository.findByFileName(fileName);
            
            if (attachmentOpt.isEmpty()) {
                throw new RuntimeException("Attachment not found: " + fileName);
            }
            
            InquiryAttachment attachment = attachmentOpt.get();
            Long inquiryId = attachment.getInquiry().getId();
            
            Path inquiryDir = fileStorageLocation.resolve("inquiry_" + inquiryId);
            Path filePath = inquiryDir.resolve(fileName).normalize();
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }
    
    /**
     * Get total storage used by all attachments
     * @return Total size in bytes
     */
    public Long getTotalStorageUsed() {
        return attachmentRepository.getTotalStorageUsed();
    }
    
    /**
     * Get total storage used by an inquiry
     * @param inquiryId The inquiry ID
     * @return Total size in bytes
     */
    public Long getTotalStorageByInquiry(Long inquiryId) {
        return attachmentRepository.getTotalAttachmentSizeByInquiryId(inquiryId);
    }
    
    /**
     * Check if inquiry has attachments
     * @param inquiryId The inquiry ID
     * @return true if has attachments
     */
    public boolean hasAttachments(Long inquiryId) {
        return attachmentRepository.existsByInquiryId(inquiryId);
    }
    
    /**
     * Get attachment count for an inquiry
     * @param inquiryId The inquiry ID
     * @return Number of attachments
     */
    public long getAttachmentCount(Long inquiryId) {
        return attachmentRepository.countByInquiryId(inquiryId);
    }
    
    // ===== Private Helper Methods =====
    
    /**
     * Validate uploaded file
     * @param file The file to validate
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            double sizeMB = file.getSize() / (1024.0 * 1024.0);
            double maxSizeMB = maxFileSize / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                String.format("File size %.2f MB exceeds maximum limit of %.2f MB", sizeMB, maxSizeMB)
            );
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: Images (JPEG, PNG, GIF), PDF, Word, Excel, Text"
            );
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename - no extension found");
        }
        
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();
        
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file extension. Allowed: .jpg, .jpeg, .png, .gif, .pdf, .doc, .docx, .xls, .xlsx, .txt"
            );
        }
    }
    
    /**
     * Generate unique filename
     * Format: inquiry_{inquiryId}_{timestamp}_{sanitized-original-name}
     * Example: inquiry_5_1729267890123_invoice.pdf
     */
    private String generateUniqueFilename(String originalFilename, Long inquiryId) {
        String cleanFilename = StringUtils.cleanPath(originalFilename);
        
        // Sanitize filename (remove special characters)
        String fileNameWithoutExt = cleanFilename.substring(0, cleanFilename.lastIndexOf("."));
        String sanitizedName = fileNameWithoutExt.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        String extension = cleanFilename.substring(cleanFilename.lastIndexOf("."));
        long timestamp = System.currentTimeMillis();
        
        return String.format("inquiry_%d_%d_%s%s", inquiryId, timestamp, sanitizedName, extension);
    }
    
    /**
     * Save file to disk
     * @param file The file to save
     * @param directory The directory to save in
     * @param filename The filename
     * @return The relative path
     */
    private String saveFile(MultipartFile file, Path directory, String filename) {
        try {
            Path targetLocation = directory.resolve(filename);
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Return relative path for database storage
            return "inquiry-attachments/" + directory.getFileName() + "/" + filename;
            
        } catch (IOException ex) {
            System.err.println("❌ Failed to save attachment: " + ex.getMessage());
            throw new RuntimeException("Failed to store file: " + filename, ex);
        }
    }
    
    /**
     * Delete physical file from disk
     * @param fileUrl The file URL/path from database
     */
    private void deletePhysicalFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract path components
            // Format: inquiry-attachments/inquiry_5/inquiry_5_123456_file.pdf
            String[] parts = fileUrl.split("/");
            if (parts.length < 3) {
                System.err.println("⚠️ Invalid file URL format: " + fileUrl);
                return;
            }
            
            String inquiryDirName = parts[parts.length - 2];
            String filename = parts[parts.length - 1];
            
            Path inquiryDir = fileStorageLocation.resolve(inquiryDirName);
            Path filePath = inquiryDir.resolve(filename).normalize();
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("✅ Deleted file: " + filename);
            } else {
                System.out.println("⚠️ File not found (may already be deleted): " + filename);
            }
            
        } catch (IOException ex) {
            System.err.println("❌ Failed to delete file: " + fileUrl + " - " + ex.getMessage());
            // Don't throw exception - file might already be deleted
        }
    }
}
