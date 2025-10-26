package com.cakify.dto;

import com.cakify.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    // Attachment information
    private List<InquiryAttachmentResponse> attachments;
    private int attachmentCount;

    // Convert from Entity to Response DTO
    public static InquiryResponse fromEntity(Inquiry inquiry) {
        InquiryResponse response = new InquiryResponse();
        response.setId(String.valueOf(inquiry.getId()));
        response.setName(inquiry.getName());
        response.setEmail(inquiry.getEmail());
        response.setMessage(inquiry.getMessage());
        response.setStatus(inquiry.getStatus().name().toLowerCase());
        response.setReply(inquiry.getReply());
        response.setDate(inquiry.getCreatedAt().toString());
        
        // Convert attachments to DTOs
        if (inquiry.getAttachments() != null && !inquiry.getAttachments().isEmpty()) {
            response.setAttachments(
                inquiry.getAttachments().stream()
                    .map(InquiryAttachmentResponse::fromEntity)
                    .collect(Collectors.toList())
            );
            response.setAttachmentCount(inquiry.getAttachments().size());
        } else {
            response.setAttachments(new ArrayList<>());
            response.setAttachmentCount(0);
        }
        
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
        return attachments != null && !attachments.isEmpty();
    }
}