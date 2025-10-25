package com.cakify.repository;

import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // Find inquiries by status
    List<Inquiry> findByStatus(InquiryStatus status);

    // Find new inquiries (unresolved)
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);

    // Find all inquiries ordered by creation date (newest first)
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // Find inquiries by email (for customer history)
    List<Inquiry> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    // Count inquiries by status
    long countByStatus(InquiryStatus status);

    // Count new inquiries (for dashboard stats)
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status = 'NEW'")
    long countNewInquiries();

    // Count total inquiries
    @Query("SELECT COUNT(i) FROM Inquiry i")
    long countTotalInquiries();

    // Find inquiries created after a specific date
    List<Inquiry> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    // Find inquiries that contain specific text in message (search functionality)
    @Query("SELECT i FROM Inquiry i WHERE LOWER(i.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> searchInquiries(@Param("searchTerm") String searchTerm);

    // Find inquiries by status with pagination support
    @Query("SELECT i FROM Inquiry i WHERE (:status IS NULL OR i.status = :status) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> findInquiriesByStatusOrdered(@Param("status") InquiryStatus status);

    // ============== NEW: Category-related queries ==============

    /**
     * Find inquiries by category
     * @param categoryId the category ID
     * @return list of inquiries in this category
     */
    List<Inquiry> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    /**
     * Find inquiries by category and status
     * @param categoryId the category ID
     * @param status the inquiry status
     * @return filtered list of inquiries
     */
    List<Inquiry> findByCategoryIdAndStatus(Long categoryId, InquiryStatus status);

    /**
     * Count inquiries by category
     * @param categoryId the category ID
     * @return number of inquiries
     */
    long countByCategoryId(Long categoryId);

    /**
     * Find inquiries without category (uncategorized)
     * @return list of uncategorized inquiries
     */
    List<Inquiry> findByCategoryIsNullOrderByCreatedAtDesc();

    /**
     * Count uncategorized inquiries
     * @return number of inquiries without category
     */
    long countByCategoryIsNull();

    // ============== NEW: Attachment-related queries ==============

    /**
     * Find inquiries with attachments
     * @return list of inquiries that have files
     */
    @Query("SELECT DISTINCT i FROM Inquiry i WHERE SIZE(i.attachments) > 0 ORDER BY i.createdAt DESC")
    List<Inquiry> findInquiriesWithAttachments();

    /**
     * Find inquiries without attachments
     * @return list of inquiries without files
     */
    @Query("SELECT i FROM Inquiry i WHERE SIZE(i.attachments) = 0 ORDER BY i.createdAt DESC")
    List<Inquiry> findInquiriesWithoutAttachments();

    /**
     * Count inquiries with attachments
     * @return number of inquiries with files
     */
    @Query("SELECT COUNT(DISTINCT i) FROM Inquiry i WHERE SIZE(i.attachments) > 0")
    long countInquiriesWithAttachments();

    // ============== NEW: Recent activity queries ==============

    /**
     * Find recent inquiries (last 10)
     * @return list of 10 most recent inquiries
     */
    List<Inquiry> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find recently replied inquiries
     * @return list of inquiries ordered by reply date
     */
    List<Inquiry> findByRepliedAtIsNotNullOrderByRepliedAtDesc();

    /**
     * Find inquiries replied within date range
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of inquiries
     */
    List<Inquiry> findByRepliedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ============== NEW: Advanced search with category ==============

    /**
     * Search inquiries with category filter
     * @param searchTerm text to search
     * @param categoryId category filter (null for all)
     * @return filtered list of inquiries
     */
    @Query("SELECT i FROM Inquiry i WHERE " +
            "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
            "(LOWER(i.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(i.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> searchInquiriesWithCategory(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId
    );

    /**
     * Get dashboard statistics per category
     * @return count of inquiries grouped by category
     */
    @Query("SELECT i.category.name, COUNT(i) FROM Inquiry i " +
            "GROUP BY i.category.name ORDER BY COUNT(i) DESC")
    List<Object[]> getInquiryCountByCategory();

    /**
     * Find resolved inquiries without reply timestamp (data consistency check)
     * @return list of inconsistent inquiries
     */
    @Query("SELECT i FROM Inquiry i WHERE i.status = 'RESOLVED' AND i.repliedAt IS NULL")
    List<Inquiry> findResolvedInquiriesWithoutReplyTimestamp();

    /**
     * Find unanswered inquiries (NEW status)
     * @return list of inquiries waiting for reply
     */
    @Query("SELECT i FROM Inquiry i WHERE i.status = 'NEW' ORDER BY i.createdAt ASC")
    List<Inquiry> findUnansweredInquiries();

    /**
     * Count inquiries created after specific date
     * @param date the date to filter from
     * @return count of recent inquiries
     */
    long countByCreatedAtAfter(LocalDateTime date);
}
