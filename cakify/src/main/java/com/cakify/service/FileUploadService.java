package com.cakify.service;

import com.cakify.dto.AttachmentResponse;
import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryAttachment;
import com.cakify.exception.FileUploadException;
import com.cakify.exception.InquiryNotFoundException;
import com.cakify.repository.InquiryAttachmentRepository;
import com.cakify.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FileUploadService - Handles file upload operations for inquiry attachments
 * 
 * OOP Principles:
 * - Encapsulation: File handling logic encapsulated
 * - Abstraction: Hides file system complexity
 * 
 * Design Patterns:
 * - Singleton Pattern: Spring creates single instance (@Service)
 * - Strategy Pattern: Different validation strategies for file types
 * - Factory Method: Uses AttachmentResponse.fromEntity()
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FileUploadService {
    
    private final InquiryAttachmentRepository attachmentRepository;
    private final InquiryRepository inquiryRepository;
    
    @Value("${file.upload.inquiry-dir:uploads/inquiries}")
    private String uploadDirectory;
    
    // File validation constants
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/png", "image/jpeg", "image/jpg"};
    private static final String ALLOWED_PDF_TYPE = "application/pdf";
    
    /**
     * Upload a file for an inquiry
     * @param file the file to upload
     * @param inquiryId the inquiry ID
     * @return attachment response
     */
    public AttachmentResponse uploadFile(MultipartFile file, Long inquiryId) {
        // Validate inquiry exists
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException(inquiryId));
        
        // Validate file
        validateFile(file);
        
        try {
            // Save file to disk
            String savedFilePath = saveFileToDisk(file, inquiryId);
            
            // Create attachment entity
            InquiryAttachment attachment = new InquiryAttachment();
            attachment.setInquiry(inquiry);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(savedFilePath);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            
            // Save to database
            InquiryAttachment saved = attachmentRepository.save(attachment);
            
            return AttachmentResponse.fromEntity(saved);
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage());
        }
    }
    
    /**
     * Upload multiple files for an inquiry
     * @param files list of files
     * @param inquiryId the inquiry ID
     * @return list of attachment responses
     */
    public List<AttachmentResponse> uploadMultipleFiles(List<MultipartFile> files, Long inquiryId) {
        return files.stream()
                .map(file -> uploadFile(file, inquiryId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all attachments for an inquiry
     * @param inquiryId the inquiry ID
     * @return list of attachments
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByInquiryId(Long inquiryId) {
        return attachmentRepository.findByInquiryId(inquiryId)
                .stream()
                .map(AttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get attachment by ID
     * @param attachmentId the attachment ID
     * @return attachment response
     */
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentById(Long attachmentId) {
        InquiryAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new FileUploadException("Attachment not found with id: " + attachmentId));
        return AttachmentResponse.fromEntity(attachment);
    }
    
    /**
     * Delete an attachment
     * @param attachmentId the attachment ID
     */
    public void deleteAttachment(Long attachmentId) {
        InquiryAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new FileUploadException("Attachment not found with id: " + attachmentId));
        
        try {
            // Delete from disk
            deleteFileFromDisk(attachment.getFilePath());
            
            // Delete from database
            attachmentRepository.delete(attachment);
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to delete file: " + e.getMessage());
        }
    }
    
    /**
     * Delete all attachments for an inquiry
     * @param inquiryId the inquiry ID
     */
    public void deleteAllAttachmentsByInquiryId(Long inquiryId) {
        List<InquiryAttachment> attachments = attachmentRepository.findByInquiryId(inquiryId);
        
        for (InquiryAttachment attachment : attachments) {
            try {
                deleteFileFromDisk(attachment.getFilePath());
            } catch (IOException e) {
                // Log error but continue deletion
                System.err.println("Failed to delete file: " + attachment.getFilePath());
            }
        }
        
        attachmentRepository.deleteByInquiryId(inquiryId);
    }
    
    /**
     * Get total storage used
     * @return total size in bytes
     */
    @Transactional(readOnly = true)
    public long getTotalStorageUsed() {
        return attachmentRepository.calculateTotalStorageUsed();
    }
    
    /**
     * Get storage used by inquiry
     * @param inquiryId the inquiry ID
     * @return size in bytes
     */
    @Transactional(readOnly = true)
    public long getStorageByInquiry(Long inquiryId) {
        return attachmentRepository.calculateStorageByInquiry(inquiryId);
    }
    
    // ============== Private Helper Methods (Encapsulation) ==============
    
    /**
     * Validate file before upload
     * @param file the file to validate
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException(
                "File size exceeds maximum allowed size of 5MB. Your file: " + 
                formatFileSize(file.getSize())
            );
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new FileUploadException(
                "File type not allowed. Allowed types: PNG, JPEG, JPG, PDF. Your file type: " + contentType
            );
        }
        
        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new FileUploadException("Invalid filename");
        }
    }
    
    /**
     * Check if file type is allowed
     * @param contentType MIME type
     * @return true if allowed
     */
    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        // Check images
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        
        // Check PDF
        return contentType.equalsIgnoreCase(ALLOWED_PDF_TYPE);
    }
    
    /**
     * Save file to disk
     * @param file the file to save
     * @param inquiryId the inquiry ID
     * @return saved file path
     */
    private String saveFileToDisk(MultipartFile file, Long inquiryId) throws IOException {
        // Create directory structure: uploads/inquiries/{inquiryId}/
        Path inquiryDir = Paths.get(uploadDirectory, String.valueOf(inquiryId));
        Files.createDirectories(inquiryDir);
        
        // Generate unique filename with timestamp
        String originalFilename = file.getOriginalFilename();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String uniqueFilename = timestamp + "_" + sanitizedFilename;
        
        // Save file
        Path targetPath = inquiryDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path
        return uploadDirectory + "/" + inquiryId + "/" + uniqueFilename;
    }
    
    /**
     * Delete file from disk
     * @param filePath the file path
     */
    private void deleteFileFromDisk(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
    
    /**
     * Sanitize filename to remove special characters
     * @param filename original filename
     * @return sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "file";
        }
        
        // Remove special characters, keep only alphanumeric, dots, underscores, and hyphens
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Format file size to human-readable string
     * @param size size in bytes
     * @return formatted string
     */
    private String formatFileSize(long size) {
        double sizeInMB = size / (1024.0 * 1024.0);
        if (sizeInMB >= 1) {
            return String.format("%.2f MB", sizeInMB);
        }
        
        double sizeInKB = size / 1024.0;
        return String.format("%.2f KB", sizeInKB);
    }
}
