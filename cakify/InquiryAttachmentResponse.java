package com.cakify.dto;

import com.cakify.entity.InquiryAttachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * Response DTO for inquiry attachments
 * Used to send attachment data to the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAttachmentResponse {
    
    private Long id;
    private Long inquiryId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String fileSizeFormatted;
    private String uploadedAt;
    private String downloadUrl;
    
    // Additional helper fields
    private boolean isImage;
    private boolean isPdf;
    private boolean isDocument;
    private String fileExtension;
    
    /**
     * Convert InquiryAttachment entity to Response DTO
     * @param attachment The entity to convert
     * @return InquiryAttachmentResponse DTO
     */
    public static InquiryAttachmentResponse fromEntity(InquiryAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        
        InquiryAttachmentResponse response = new InquiryAttachmentResponse();
        response.setId(attachment.getId());
        response.setInquiryId(attachment.getInquiry().getId());
        response.setFileName(attachment.getFileName());
        response.setFileUrl(attachment.getFileUrl());
        response.setFileType(attachment.getFileType());
        response.setFileSize(attachment.getFileSize());
        response.setFileSizeFormatted(attachment.getFormattedFileSize());
        
        // Format upload date
        if (attachment.getUploadedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            response.setUploadedAt(attachment.getUploadedAt().format(formatter));
        }
        
        // Generate download URL
        response.setDownloadUrl("/api/inquiries/attachments/download/" + attachment.getFileName());
        
        // Set helper fields
        response.setImage(attachment.isImage());
        response.setPdf(attachment.isPdf());
        response.setDocument(attachment.isDocument());
        response.setFileExtension(attachment.getFileExtension());
        
        return response;
    }
    
    /**
     * Convert InquiryAttachment entity to Response DTO with custom download URL base
     * @param attachment The entity to convert
     * @param downloadUrlBase The base URL for downloads
     * @return InquiryAttachmentResponse DTO
     */
    public static InquiryAttachmentResponse fromEntity(InquiryAttachment attachment, String downloadUrlBase) {
        InquiryAttachmentResponse response = fromEntity(attachment);
        if (response != null && downloadUrlBase != null) {
            response.setDownloadUrl(downloadUrlBase + "/" + attachment.getFileName());
        }
        return response;
    }
    
    /**
     * Get file type category for UI display
     * @return Category name: "image", "pdf", "document", or "other"
     */
    public String getFileTypeCategory() {
        if (isImage) {
            return "image";
        } else if (isPdf) {
            return "pdf";
        } else if (isDocument) {
            return "document";
        } else {
            return "other";
        }
    }
    
    /**
     * Get icon name based on file type (for frontend use)
     * @return Icon name string
     */
    public String getFileIcon() {
        if (isImage) {
            return "image";
        } else if (isPdf) {
            return "pdf";
        } else if (isDocument) {
            return "document";
        } else {
            return "file";
        }
    }
    
    /**
     * Check if file is displayable in browser
     * @return true if file can be displayed inline
     */
    public boolean isDisplayableInBrowser() {
        return isImage || isPdf;
    }
}
