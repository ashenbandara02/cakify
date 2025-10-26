package com.cakify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * InquiryCategory Entity - Represents categories for organizing customer inquiries
 * 
 * OOP Principles Applied:
 * - Encapsulation: Private fields with controlled access via getters/setters
 * - Abstraction: Hides internal category management complexity
 * 
 * Design Patterns:
 * - Builder Pattern: Lombok @Data provides builder capability
 * - Repository Pattern: Used with InquiryCategoryRepository for data access
 * 
 * Performance Optimizations:
 * - Database indexes on is_active and display_order for faster filtering and sorting
 */
@Entity
@Table(name = "inquiry_categories", indexes = {
    @Index(name = "idx_category_active", columnList = "is_active"),
    @Index(name = "idx_category_display_order", columnList = "display_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be less than 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Size(max = 255, message = "Description must be less than 255 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Size(max = 50, message = "Icon name must be less than 50 characters")
    @Column(length = 50)
    private String icon; // Icon identifier for frontend (e.g., "help-circle", "shopping-cart")
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationship: One category can have many inquiries
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inquiry> inquiries = new ArrayList<>();
    
    // ============== Helper Methods (Encapsulation) ==============
    
    /**
     * Check if this category is currently active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }
    
    /**
     * Activate this category
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * Deactivate this category
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * Toggle the active status
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }
    
    /**
     * Get the count of inquiries in this category
     * @return number of inquiries
     */
    public int getInquiryCount() {
        return this.inquiries != null ? this.inquiries.size() : 0;
    }
    
    /**
     * Check if this category has any inquiries
     * @return true if there are inquiries, false otherwise
     */
    public boolean hasInquiries() {
        return this.inquiries != null && !this.inquiries.isEmpty();
    }
    
    /**
     * Add an inquiry to this category
     * @param inquiry the inquiry to add
     */
    public void addInquiry(Inquiry inquiry) {
        if (this.inquiries == null) {
            this.inquiries = new ArrayList<>();
        }
        this.inquiries.add(inquiry);
        inquiry.setCategory(this);
    }
    
    /**
     * Remove an inquiry from this category
     * @param inquiry the inquiry to remove
     */
    public void removeInquiry(Inquiry inquiry) {
        if (this.inquiries != null) {
            this.inquiries.remove(inquiry);
            inquiry.setCategory(null);
        }
    }
    
    /**
     * Check if category can be deleted (no active inquiries)
     * @return true if safe to delete, false otherwise
     */
    public boolean canBeDeleted() {
        return !hasInquiries();
    }
    
    /**
     * Get display name for UI
     * @return formatted display name
     */
    public String getDisplayName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return "InquiryCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                ", isActive=" + isActive +
                ", inquiryCount=" + getInquiryCount() +
                '}';
    }
}
