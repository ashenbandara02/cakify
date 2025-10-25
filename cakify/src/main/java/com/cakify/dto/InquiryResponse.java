package com.cakify.dto;

import com.cakify.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * InquiryResponse DTO - Response object for inquiries
 * 
 * Design Patterns:
 * - Factory Method Pattern: fromEntity() creates DTO from entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {

    private String id;
    private String name;
    private String email;
    private String message;
    private String status;
    private String reply;
    private String date;
    
    // NEW: Category information
    private Long categoryId;
    private String categoryName;
    
    // NEW: Attachment information
    private Integer attachmentCount;
    private List<AttachmentResponse> attachments;
    
    // NEW: Reply timestamp
    private String repliedAt;

    /**
     * Factory Method: Convert from Entity to Response DTO
     * @param inquiry the entity to convert
     * @return InquiryResponse DTO
     */
    public static InquiryResponse fromEntity(Inquiry inquiry) {
        InquiryResponse response = new InquiryResponse();
        response.setId(String.valueOf(inquiry.getId()));
        response.setName(inquiry.getName());
        response.setEmail(inquiry.getEmail());
        response.setMessage(inquiry.getMessage());
        response.setStatus(inquiry.getStatus().name().toLowerCase());
        response.setReply(inquiry.getReply());
        response.setDate(inquiry.getCreatedAt().toString());
        
        // NEW: Category information
        if (inquiry.getCategory() != null) {
            response.setCategoryId(inquiry.getCategory().getId());
            response.setCategoryName(inquiry.getCategory().getName());
        } else {
            response.setCategoryName("Uncategorized");
        }
        
        // NEW: Attachment information
        response.setAttachmentCount(inquiry.getAttachmentCount());
        if (inquiry.getAttachments() != null) {
            response.setAttachments(
                inquiry.getAttachments().stream()
                    .map(AttachmentResponse::fromEntity)
                    .collect(Collectors.toList())
            );
        } else {
            response.setAttachments(new ArrayList<>());
        }
        
        // NEW: Reply timestamp
        if (inquiry.getRepliedAt() != null) {
            response.setRepliedAt(inquiry.getRepliedAt().toString());
        }
        
        return response;
    }
    
    /**
     * Factory Method: Create simple InquiryResponse (without attachments)
     * @param inquiry the entity to convert
     * @return simplified InquiryResponse
     */
    public static InquiryResponse fromEntitySimple(Inquiry inquiry) {
        InquiryResponse response = new InquiryResponse();
        response.setId(String.valueOf(inquiry.getId()));
        response.setName(inquiry.getName());
        response.setEmail(inquiry.getEmail());
        response.setMessage(inquiry.getMessage());
        response.setStatus(inquiry.getStatus().name().toLowerCase());
        response.setReply(inquiry.getReply());
        response.setDate(inquiry.getCreatedAt().toString());
        response.setCategoryName(inquiry.getCategoryName());
        response.setAttachmentCount(inquiry.getAttachmentCount());
        return response;
    }

    // Helper methods
    public boolean isResolved() {
        return "resolved".equalsIgnoreCase(this.status);
    }

    public boolean hasReply() {
        return this.reply != null && !this.reply.trim().isEmpty();
    }
    
    public boolean hasAttachments() {
        return this.attachmentCount != null && this.attachmentCount > 0;
    }
    
    public boolean hasCategory() {
        return this.categoryId != null;
    }
}