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

/**
 * Inquiry Entity - Represents customer inquiries/support tickets
 *
 * OOP Principles Applied:
 * - Encapsulation: Private fields with controlled access via getters/setters
 * - Abstraction: Helper methods hide business logic complexity
 * - Polymorphism: Inherits from JPA entity behaviors
 *
 * Design Patterns:
 * - Builder Pattern: Lombok @Data provides builder capability
 * - Repository Pattern: Used with InquiryRepository for data access
 * - Bridge Pattern: Service layer bridges controller and repository
 */
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

    // NEW: Timestamp when admin replied to inquiry
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    // ============== Relationships (Encapsulation via ORM) ==============

    // Relationship: Many inquiries belong to one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private InquiryCategory category;

    // Relationship: One inquiry can have many attachments
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InquiryAttachment> attachments = new ArrayList<>();

    // ============== Helper Methods (Encapsulation) ==============

    /**
     * Check if inquiry is resolved
     * @return true if resolved, false otherwise
     */
    public boolean isResolved() {
        return this.status == InquiryStatus.RESOLVED;
    }

    /**
     * Mark inquiry as resolved with admin reply
     * Sets status to RESOLVED, stores reply, and records replied timestamp
     * @param adminReply the admin's response message
     */
    public void markAsResolved(String adminReply) {
        this.status = InquiryStatus.RESOLVED;
        this.reply = adminReply;
        this.repliedAt = LocalDateTime.now(); // NEW: Record when replied
    }

    /**
     * Check if inquiry has a reply from admin
     * @return true if reply exists, false otherwise
     */
    public boolean hasReply() {
        return this.reply != null && !this.reply.trim().isEmpty();
    }

    // ============== NEW: Category Management Methods ==============

    /**
     * Set the category for this inquiry
     * @param category the category to assign
     */
    public void setCategory(InquiryCategory category) {
        this.category = category;
    }

    /**
     * Get the category name (null-safe)
     * @return category name or "Uncategorized"
     */
    public String getCategoryName() {
        return this.category != null ? this.category.getName() : "Uncategorized";
    }

    /**
     * Check if inquiry has a category assigned
     * @return true if category exists, false otherwise
     */
    public boolean hasCategory() {
        return this.category != null;
    }

    // ============== NEW: Attachment Management Methods ==============

    /**
     * Add an attachment to this inquiry
     * @param attachment the attachment to add
     */
    public void addAttachment(InquiryAttachment attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
        attachment.setInquiry(this);
    }

    /**
     * Remove an attachment from this inquiry
     * @param attachment the attachment to remove
     */
    public void removeAttachment(InquiryAttachment attachment) {
        if (this.attachments != null) {
            this.attachments.remove(attachment);
            attachment.setInquiry(null);
        }
    }

    /**
     * Get count of attachments
     * @return number of attachments
     */
    public int getAttachmentCount() {
        return this.attachments != null ? this.attachments.size() : 0;
    }

    /**
     * Check if inquiry has attachments
     * @return true if attachments exist, false otherwise
     */
    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }

    /**
     * Get all attachments (defensive copy)
     * @return list of attachments
     */
    public List<InquiryAttachment> getAttachments() {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        return this.attachments;
    }

    /**
     * Check if inquiry was replied to
     * @return true if replied, false otherwise
     */
    public boolean wasReplied() {
        return this.repliedAt != null;
    }

    @Override
    public String toString() {
        return "Inquiry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", category=" + getCategoryName() +
                ", attachmentCount=" + getAttachmentCount() +
                ", hasReply=" + hasReply() +
                ", createdAt=" + createdAt +
                '}';
    }
}
