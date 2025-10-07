package com.cakify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "Message is required")
    @Size(min = 10, message = "Message must be at least 10 characters long")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.NEW;
    
    @Column(columnDefinition = "TEXT")
    private String reply;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean isResolved() {
        return this.status == InquiryStatus.RESOLVED;
    }
    
    public void markAsResolved(String adminReply) {
        this.status = InquiryStatus.RESOLVED;
        this.reply = adminReply;
    }
    
    public boolean hasReply() {
        return this.reply != null && !this.reply.trim().isEmpty();
    }
}