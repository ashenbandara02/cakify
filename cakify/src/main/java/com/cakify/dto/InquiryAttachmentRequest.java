package com.cakify.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for uploading inquiry attachments
 * Note: The file itself is sent as MultipartFile, not in this DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAttachmentRequest {
    
    @NotNull(message = "Inquiry ID is required")
    private Long inquiryId;
    
    // Optional: Additional metadata can be added here
    private String description;
    
    private String tags;
}
