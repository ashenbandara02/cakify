package com.cakify.dto;

import com.cakify.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
        return response;
    }
    
    // Helper methods
    public boolean isResolved() {
        return "resolved".equalsIgnoreCase(this.status);
    }
    
    public boolean hasReply() {
        return this.reply != null && !this.reply.trim().isEmpty();
    }
}