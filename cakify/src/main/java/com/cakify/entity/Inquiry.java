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
import java.util.ArrayList;
import java.util.List;

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
    
    // One Inquiry has Many Attachments
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InquiryAttachment> attachments = new ArrayList<>();
    
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
    
    // Attachment management helper methods
    
    /**
     * Add an attachment to this inquiry (maintains bidirectional relationship)
     */
    public void addAttachment(InquiryAttachment attachment) {
        attachments.add(attachment);
        attachment.setInquiry(this);
    }
    
    /**
     * Remove an attachment from this inquiry (maintains bidirectional relationship)
     */
    public void removeAttachment(InquiryAttachment attachment) {
        attachments.remove(attachment);
        attachment.setInquiry(null);
    }
    
    /**
     * Check if this inquiry has any attachments
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
    
    /**
     * Get the number of attachments
     */
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }
    
    /**
     * Get total size of all attachments in bytes
     */
    public long getTotalAttachmentSize() {
        if (attachments == null || attachments.isEmpty()) {
            return 0L;
        }
        return attachments.stream()
                .mapToLong(att -> att.getFileSize() != null ? att.getFileSize() : 0L)
                .sum();
    }
    
    /**
     * Check if inquiry has image attachments
     */
    public boolean hasImageAttachments() {
        if (attachments == null || attachments.isEmpty()) {
            return false;
        }
        return attachments.stream().anyMatch(InquiryAttachment::isImage);
    }
}