package com.cakify.repository;

import com.cakify.entity.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InquiryCategoryRepository - Data access layer for InquiryCategory entity
 *
 * Design Patterns:
 * - Repository Pattern: Abstracts data access logic
 * - Singleton Pattern: Spring creates single instance
 */
@Repository
public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {

    /**
     * Find all active categories
     * @return list of active categories
     */
    List<InquiryCategory> findByIsActiveTrue();

    /**
     * Find all categories ordered by display order
     * @return sorted list of categories
     */
    List<InquiryCategory> findAllByOrderByDisplayOrderAsc();

    /**
     * Find active categories ordered by display order
     * @return sorted list of active categories
     */
    List<InquiryCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Check if category name already exists (case-insensitive)
     * @param name the category name to check
     * @return true if exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find category by name (case-insensitive)
     * @param name the category name
     * @return Optional containing category if found
     */
    Optional<InquiryCategory> findByNameIgnoreCase(String name);

    /**
     * Count inquiries per category
     * @param categoryId the category ID
     * @return number of inquiries in this category
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.category.id = :categoryId")
    long countInquiriesByCategory(@Param("categoryId") Long categoryId);

    /**
     * Find categories with inquiry count
     * @return list of categories with inquiry statistics
     */
    @Query("SELECT c FROM InquiryCategory c LEFT JOIN c.inquiries i GROUP BY c ORDER BY c.displayOrder ASC")
    List<InquiryCategory> findAllWithInquiryCount();

    /**
     * Count total active categories
     * @return number of active categories
     */
    long countByIsActiveTrue();
}
