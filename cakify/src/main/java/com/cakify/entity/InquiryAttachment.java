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

@Entity
@Table(name = "inquiry_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAttachment{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    @NotNull(message = "Inquiry is required")
    private Inquiry inquiry;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must be less than 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must be less than 500 characters")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Size(max = 500, message = "File URL must be less than 500 characters")
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Size(max = 100, message = "File type must be less than 100 characters")
    @Column(name = "file_type", length = 100)
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    // Helper methods
    
    /**
     * Get formatted file size (e.g., "2.5 MB", "340 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Check if the attachment is an image
     */
    public boolean isImage() {
        if (fileType == null) {
            return false;
        }
        return fileType.toLowerCase().startsWith("image/");
    }
    
    /**
     * Check if the attachment is a PDF
     */
    public boolean isPdf() {
        if (fileType == null) {
            return false;
        }
        return fileType.equalsIgnoreCase("application/pdf");
    }
    
    /**
     * Check if the attachment is a document (Word, etc.)
     */
    public boolean isDocument() {
        if (fileType == null) {
            return false;
        }
        String lowerType = fileType.toLowerCase();
        return lowerType.contains("word") || 
               lowerType.contains("document") ||
               lowerType.contains("msword") ||
               lowerType.contains("openxmlformats");
    }
    
    /**
     * Get file extension from file name
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Validate file size against maximum allowed (in bytes)
     */
    public boolean isFileSizeValid(long maxSizeInBytes) {
        return fileSize != null && fileSize <= maxSizeInBytes;
    }
}
