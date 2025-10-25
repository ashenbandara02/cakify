package com.cakify.dto;

import com.cakify.entity.InquiryAttachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AttachmentResponse DTO - Response object for inquiry attachments
 * 
 * Design Patterns:
 * - Factory Method Pattern: fromEntity() creates DTO from entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    
    private Long id;
    private String fileName;
    private String fileUrl;
    private String downloadUrl;
    private String fileType;
    private Long fileSize;
    private String fileSizeFormatted;
    private Double fileSizeInMB;
    private String fileIcon;
    private Boolean isImage;
    private Boolean isPdf;
    private LocalDateTime uploadedAt;
    
    /**
     * Factory Method: Create AttachmentResponse from InquiryAttachment entity
     * @param attachment the entity to convert
     * @return AttachmentResponse DTO
     */
    public static AttachmentResponse fromEntity(InquiryAttachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setFileName(attachment.getFileName());
        response.setFileUrl(attachment.getFilePath());
        response.setDownloadUrl(attachment.getDownloadUrl());
        response.setFileType(attachment.getFileType());
        response.setFileSize(attachment.getFileSize());
        response.setFileSizeFormatted(attachment.getFormattedFileSize());
        response.setFileSizeInMB(attachment.getFileSizeInMB());
        response.setFileIcon(attachment.getFileIcon());
        response.setIsImage(attachment.isImage());
        response.setIsPdf(attachment.isPdf());
        response.setUploadedAt(attachment.getUploadedAt());
        return response;
    }
}
