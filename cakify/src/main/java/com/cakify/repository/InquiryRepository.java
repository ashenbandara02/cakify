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
}