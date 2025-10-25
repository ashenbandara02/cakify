package com.cakify.repository;

import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Find inquiries by status with pagination
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);
    
    // Find new inquiries (unresolved)
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    
    // Find all inquiries ordered by creation date (newest first)
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    
    // Find all inquiries with pagination (automatic sorting by createdAt desc will be applied via Pageable)
    Page<Inquiry> findAll(Pageable pageable);
    
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
    
    // ============== ANALYTICS: Time-based queries ==============
    
    /**
     * Get inquiries grouped by month for trend analysis
     * Returns array: [year-month, count]
     * @param startDate start date for analysis
     * @return monthly inquiry counts
     */
    @Query("SELECT FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM'), COUNT(i) " +
           "FROM Inquiry i WHERE i.createdAt >= :startDate " +
           "GROUP BY FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM') " +
           "ORDER BY FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM')")
    List<Object[]> getInquiriesPerMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get inquiries grouped by week for recent trend analysis
     * @param startDate start date for analysis
     * @return weekly inquiry counts
     */
    @Query("SELECT FUNCTION('TO_CHAR', i.createdAt, 'IYYY-IW'), COUNT(i) " +
           "FROM Inquiry i WHERE i.createdAt >= :startDate " +
           "GROUP BY FUNCTION('TO_CHAR', i.createdAt, 'IYYY-IW') " +
           "ORDER BY FUNCTION('TO_CHAR', i.createdAt, 'IYYY-IW')")
    List<Object[]> getInquiriesPerWeek(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get inquiries grouped by day for recent activity
     * @param startDate start date for analysis
     * @return daily inquiry counts
     */
    @Query("SELECT FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM-DD'), COUNT(i) " +
           "FROM Inquiry i WHERE i.createdAt >= :startDate " +
           "GROUP BY FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM-DD') " +
           "ORDER BY FUNCTION('TO_CHAR', i.createdAt, 'YYYY-MM-DD')")
    List<Object[]> getInquiriesPerDay(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get category distribution for pie/donut charts
     * Returns: [categoryName, count, percentage]
     */
    @Query("SELECT c.name, COUNT(i), " +
           "CAST(COUNT(i) * 100.0 / (SELECT COUNT(i2) FROM Inquiry i2) AS double) " +
           "FROM Inquiry i LEFT JOIN i.category c " +
           "GROUP BY c.name " +
           "ORDER BY COUNT(i) DESC")
    List<Object[]> getCategoryDistribution();
    
    /**
     * Get status distribution for dashboard
     * Returns: [status, count]
     */
    @Query("SELECT i.status, COUNT(i) FROM Inquiry i GROUP BY i.status ORDER BY COUNT(i) DESC")
    List<Object[]> getStatusDistribution();
    
    // ============== ANALYTICS: Response time metrics ==============
    
    /**
     * Get average response time in hours
     * Only for resolved inquiries
     */
    @Query("SELECT AVG(CAST(FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) AS double)) " +
           "FROM Inquiry i WHERE i.repliedAt IS NOT NULL")
    Double getAverageResponseTimeInHours();
    
    /**
     * Get median response time (approximation using percentile)
     */
    @Query("SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt))) " +
           "FROM Inquiry i WHERE i.repliedAt IS NOT NULL")
    Double getMedianResponseTimeInHours();
    
    /**
     * Get response time by category for performance comparison
     * Returns: [categoryName, avgResponseTimeHours]
     */
    @Query("SELECT c.name, AVG(CAST(FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) AS double)) " +
           "FROM Inquiry i JOIN i.category c " +
           "WHERE i.repliedAt IS NOT NULL " +
           "GROUP BY c.name " +
           "ORDER BY AVG(CAST(FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) AS double))")
    List<Object[]> getAverageResponseTimeByCategory();
    
    /**
     * Get fastest and slowest response times
     * Returns: [min, max] in hours
     */
    @Query("SELECT " +
           "MIN(CAST(FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) AS double)), " +
           "MAX(CAST(FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) AS double)) " +
           "FROM Inquiry i WHERE i.repliedAt IS NOT NULL")
    Object[] getResponseTimeRange();
    
    /**
     * Get inquiries with slow response time (> 48 hours)
     */
    @Query("SELECT i FROM Inquiry i WHERE i.repliedAt IS NOT NULL " +
           "AND FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', i.repliedAt, i.createdAt)) > 172800 " +
           "ORDER BY FUNCTION('AGE', i.repliedAt, i.createdAt) DESC")
    List<Inquiry> getSlowResponseInquiries();
    
    /**
     * Count inquiries still pending beyond threshold (e.g., 24 hours)
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status = 'NEW' " +
           "AND FUNCTION('DATE_PART', 'epoch', FUNCTION('AGE', CURRENT_TIMESTAMP, i.createdAt)) > :secondsThreshold")
    long countOverdueInquiries(@Param("secondsThreshold") int secondsThreshold);
    
    // ============== ANALYTICS: Trend analysis ==============
    
    /**
     * Compare current month vs previous month inquiry count
     * Returns: [currentMonthCount, previousMonthCount, percentageChange]
     */
    @Query("SELECT " +
           "(SELECT COUNT(i1) FROM Inquiry i1 WHERE FUNCTION('TO_CHAR', i1.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE, 'YYYY-MM')), " +
           "(SELECT COUNT(i2) FROM Inquiry i2 WHERE FUNCTION('TO_CHAR', i2.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE - 1 MONTH, 'YYYY-MM')), " +
           "CASE WHEN (SELECT COUNT(i2) FROM Inquiry i2 WHERE FUNCTION('TO_CHAR', i2.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE - 1 MONTH, 'YYYY-MM')) > 0 " +
           "THEN CAST(((SELECT COUNT(i1) FROM Inquiry i1 WHERE FUNCTION('TO_CHAR', i1.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE, 'YYYY-MM')) - " +
           "(SELECT COUNT(i2) FROM Inquiry i2 WHERE FUNCTION('TO_CHAR', i2.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE - 1 MONTH, 'YYYY-MM'))) * 100.0 / " +
           "(SELECT COUNT(i2) FROM Inquiry i2 WHERE FUNCTION('TO_CHAR', i2.createdAt, 'YYYY-MM') = FUNCTION('TO_CHAR', CURRENT_DATE - 1 MONTH, 'YYYY-MM')) AS double) " +
           "ELSE 0 END")
    List<Object> getMonthlyTrend();
    
    /**
     * Get busiest hours of the day (for staffing optimization)
     * Returns: [hour, count]
     */
    @Query("SELECT FUNCTION('DATE_PART', 'hour', i.createdAt), COUNT(i) " +
           "FROM Inquiry i " +
           "GROUP BY FUNCTION('DATE_PART', 'hour', i.createdAt) " +
           "ORDER BY COUNT(i) DESC")
    List<Object[]> getBusiestHours();
    
    /**
     * Get busiest days of the week
     * Returns: [dayOfWeek, count]
     */
    @Query("SELECT FUNCTION('DATE_PART', 'dow', i.createdAt), COUNT(i) " +
           "FROM Inquiry i " +
           "GROUP BY FUNCTION('DATE_PART', 'dow', i.createdAt) " +
           "ORDER BY COUNT(i) DESC")
    List<Object[]> getBusiestDaysOfWeek();
}