package com.cakify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * InquiryAttachment Entity - Represents file attachments for customer inquiries
 * 
 * OOP Principles Applied:
 * - Encapsulation: Private fields with controlled access
 * - Abstraction: Hides file storage complexity from business logic
 * 
 * Design Patterns:
 * - Builder Pattern: Lombok @Data provides builder capability
 * - Repository Pattern: Used with InquiryAttachmentRepository
 */
@Entity
@Table(name = "inquiry_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relationship: Many attachments belong to one inquiry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    @NotNull(message = "Inquiry reference is required")
    private Inquiry inquiry;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must be less than 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must be less than 500 characters")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @NotBlank(message = "File type is required")
    @Size(max = 50, message = "File type must be less than 50 characters")
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType; // MIME type: image/png, image/jpeg, application/pdf
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // Size in bytes
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    // ============== Helper Methods (Encapsulation) ==============
    
    /**
     * Get file size in megabytes (MB)
     * @return file size in MB
     */
    public double getFileSizeInMB() {
        if (this.fileSize == null) {
            return 0.0;
        }
        return this.fileSize / (1024.0 * 1024.0);
    }
    
    /**
     * Get file size in kilobytes (KB)
     * @return file size in KB
     */
    public double getFileSizeInKB() {
        if (this.fileSize == null) {
            return 0.0;
        }
        return this.fileSize / 1024.0;
    }
    
    /**
     * Get formatted file size string
     * @return human-readable file size
     */
    public String getFormattedFileSize() {
        if (this.fileSize == null) {
            return "0 B";
        }
        
        double sizeInMB = getFileSizeInMB();
        if (sizeInMB >= 1) {
            return String.format("%.2f MB", sizeInMB);
        }
        
        double sizeInKB = getFileSizeInKB();
        if (sizeInKB >= 1) {
            return String.format("%.2f KB", sizeInKB);
        }
        
        return this.fileSize + " B";
    }
    
    /**
     * Check if the file is an image
     * @return true if image, false otherwise
     */
    public boolean isImage() {
        if (this.fileType == null) {
            return false;
        }
        return this.fileType.startsWith("image/");
    }
    
    /**
     * Check if the file is a PDF
     * @return true if PDF, false otherwise
     */
    public boolean isPdf() {
        if (this.fileType == null) {
            return false;
        }
        return this.fileType.equalsIgnoreCase("application/pdf");
    }
    
    /**
     * Check if file type is allowed
     * Allowed types: image/png, image/jpeg, image/jpg, application/pdf
     * @return true if allowed, false otherwise
     */
    public boolean isAllowedFileType() {
        if (this.fileType == null) {
            return false;
        }
        
        return this.fileType.equalsIgnoreCase("image/png") ||
               this.fileType.equalsIgnoreCase("image/jpeg") ||
               this.fileType.equalsIgnoreCase("image/jpg") ||
               this.fileType.equalsIgnoreCase("application/pdf");
    }
    
    /**
     * Check if file size exceeds maximum allowed (5MB)
     * @return true if exceeds, false otherwise
     */
    public boolean exceedsMaxSize() {
        if (this.fileSize == null) {
            return false;
        }
        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        return this.fileSize > maxSizeInBytes;
    }
    
    /**
     * Get file extension from filename
     * @return file extension (e.g., "pdf", "png")
     */
    public String getFileExtension() {
        if (this.fileName == null || !this.fileName.contains(".")) {
            return "";
        }
        return this.fileName.substring(this.fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Get download URL path
     * @return URL path for downloading this file
     */
    public String getDownloadUrl() {
        return "/api/inquiries/attachments/" + this.id + "/download";
    }
    
    /**
     * Get file icon based on file type
     * @return icon identifier for frontend
     */
    public String getFileIcon() {
        if (isImage()) {
            return "image";
        } else if (isPdf()) {
            return "pdf";
        }
        return "file";
    }
    
    /**
     * Check if attachment belongs to a specific inquiry
     * @param inquiryId the inquiry ID to check
     * @return true if belongs to inquiry, false otherwise
     */
    public boolean belongsToInquiry(Long inquiryId) {
        return this.inquiry != null && 
               this.inquiry.getId() != null && 
               this.inquiry.getId().equals(inquiryId);
    }
    
    @Override
    public String toString() {
        return "InquiryAttachment{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + getFormattedFileSize() +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
