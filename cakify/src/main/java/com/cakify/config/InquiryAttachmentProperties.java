package com.cakify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for Inquiry Attachment functionality.
 * Reads properties from application.properties with prefix "inquiry.attachment"
 */
@Configuration
@ConfigurationProperties(prefix = "inquiry.attachment")
@Getter
@Setter
public class InquiryAttachmentProperties {

    /**
     * Directory where attachment files will be stored
     * Default: uploads/inquiry-attachments
     */
    private String uploadDir = "uploads/inquiry-attachments";

    /**
     * Maximum file size in bytes (default: 10MB)
     * Default: 10485760 bytes (10 * 1024 * 1024)
     */
    private long maxFileSize = 10485760L;

    /**
     * Maximum number of files allowed per inquiry
     * Default: 5
     */
    private int maxFilesPerInquiry = 5;

    /**
     * List of allowed file MIME types
     * Default: Common image, PDF, and document types
     */
    private List<String> allowedTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Get max file size in MB for display purposes
     */
    public long getMaxFileSizeMB() {
        return maxFileSize / (1024 * 1024);
    }

    /**
     * Check if a file type is allowed
     */
    public boolean isFileTypeAllowed(String mimeType) {
        return allowedTypes.contains(mimeType);
    }

    /**
     * Validate file size
     */
    public boolean isFileSizeValid(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSize;
    }
}
