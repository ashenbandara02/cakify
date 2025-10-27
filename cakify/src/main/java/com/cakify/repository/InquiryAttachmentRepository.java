package com.cakify.repository;

import com.cakify.entity.InquiryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryAttachmentRepository extends JpaRepository<InquiryAttachment, Long> {
    
    /**
     * Find all attachments for a specific inquiry, ordered by upload date (newest first)
     * @param inquiryId The ID of the inquiry
     * @return List of attachments for the inquiry
     */
    List<InquiryAttachment> findByInquiryIdOrderByUploadedAtDesc(Long inquiryId);
    
    /**
     * Find all attachments for a specific inquiry, ordered by upload date (oldest first)
     * @param inquiryId The ID of the inquiry
     * @return List of attachments for the inquiry
     */
    List<InquiryAttachment> findByInquiryIdOrderByUploadedAtAsc(Long inquiryId);
    
    /**
     * Find attachments by file type (e.g., "image/jpeg", "application/pdf")
     * @param fileType The MIME type to search for
     * @return List of attachments matching the file type
     */
    List<InquiryAttachment> findByFileTypeContaining(String fileType);
    
    /**
     * Find attachments by inquiry and file type
     * @param inquiryId The ID of the inquiry
     * @param fileType The file type to search for
     * @return List of attachments matching both inquiry and file type
     */
    List<InquiryAttachment> findByInquiryIdAndFileTypeContaining(Long inquiryId, String fileType);
    
    /**
     * Count total attachments for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @return Number of attachments
     */
    long countByInquiryId(Long inquiryId);
    
    /**
     * Count attachments by file type
     * @param fileType The file type to count
     * @return Number of attachments with that file type
     */
    long countByFileTypeContaining(String fileType);
    
    /**
     * Delete all attachments for a specific inquiry
     * @param inquiryId The ID of the inquiry
     */
    @Transactional
    void deleteByInquiryId(Long inquiryId);
    
    /**
     * Find attachment by file URL
     * @param fileUrl The file URL to search for
     * @return Optional containing the attachment if found
     */
    Optional<InquiryAttachment> findByFileUrl(String fileUrl);
    
    /**
     * Find attachment by file name
     * @param fileName The file name to search for
     * @return Optional containing the attachment if found
     */
    Optional<InquiryAttachment> findByFileName(String fileName);
    
    /**
     * Find attachments uploaded after a specific date
     * @param date The date to compare
     * @return List of attachments uploaded after the date
     */
    List<InquiryAttachment> findByUploadedAtAfterOrderByUploadedAtDesc(LocalDateTime date);
    
    /**
     * Find attachments within a date range for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @param startDate The start date
     * @param endDate The end date
     * @return List of attachments within the date range
     */
    List<InquiryAttachment> findByInquiryIdAndUploadedAtBetween(
        Long inquiryId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    /**
     * Find attachments larger than a specific size
     * @param fileSize The minimum file size in bytes
     * @return List of attachments larger than the specified size
     */
    List<InquiryAttachment> findByFileSizeGreaterThan(Long fileSize);
    
    /**
     * Find image attachments for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @return List of image attachments
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.inquiry.id = :inquiryId " +
           "AND LOWER(a.fileType) LIKE 'image/%' ORDER BY a.uploadedAt DESC")
    List<InquiryAttachment> findImageAttachmentsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * Find document attachments (PDF, Word, etc.) for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @return List of document attachments
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.inquiry.id = :inquiryId " +
           "AND (LOWER(a.fileType) LIKE '%pdf%' OR LOWER(a.fileType) LIKE '%word%' " +
           "OR LOWER(a.fileType) LIKE '%document%' OR LOWER(a.fileType) LIKE '%msword%') " +
           "ORDER BY a.uploadedAt DESC")
    List<InquiryAttachment> findDocumentAttachmentsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * Get total size of all attachments for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @return Total size in bytes
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM InquiryAttachment a WHERE a.inquiry.id = :inquiryId")
    Long getTotalAttachmentSizeByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * Get total count of all attachments in the system
     * @return Total number of attachments
     */
    @Query("SELECT COUNT(a) FROM InquiryAttachment a")
    long countTotalAttachments();
    
    /**
     * Get total storage used by all attachments
     * @return Total storage in bytes
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM InquiryAttachment a")
    Long getTotalStorageUsed();
    
    /**
     * Find inquiries that have attachments
     * @return List of inquiry IDs that have at least one attachment
     */
    @Query("SELECT DISTINCT a.inquiry.id FROM InquiryAttachment a ORDER BY a.inquiry.id")
    List<Long> findInquiryIdsWithAttachments();
    
    /**
     * Check if an inquiry has attachments
     * @param inquiryId The ID of the inquiry
     * @return true if the inquiry has at least one attachment, false otherwise
     */
    boolean existsByInquiryId(Long inquiryId);
    
    /**
     * Find the latest attachment for a specific inquiry
     * @param inquiryId The ID of the inquiry
     * @return Optional containing the latest attachment if found
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.inquiry.id = :inquiryId " +
           "ORDER BY a.uploadedAt DESC LIMIT 1")
    Optional<InquiryAttachment> findLatestAttachmentByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * Find attachments by file extension (from file name)
     * @param extension The file extension (e.g., "jpg", "pdf")
     * @return List of attachments with that extension
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE LOWER(a.fileName) LIKE LOWER(CONCAT('%', :extension)) " +
           "ORDER BY a.uploadedAt DESC")
    List<InquiryAttachment> findByFileExtension(@Param("extension") String extension);
    
    /**
     * Search attachments by file name (case-insensitive, partial match)
     * @param searchTerm The search term
     * @return List of attachments matching the search term
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE LOWER(a.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.uploadedAt DESC")
    List<InquiryAttachment> searchByFileName(@Param("searchTerm") String searchTerm);
    
    /**
     * Find all attachments with pagination support
     * @return List of all attachments ordered by upload date (newest first)
     */
    List<InquiryAttachment> findAllByOrderByUploadedAtDesc();
    
    /**
     * Delete attachment by file name
     * @param fileName The file name to delete
     */
    @Transactional
    void deleteByFileName(String fileName);
    
    /**
     * Delete attachment by file URL
     * @param fileUrl The file URL to delete
     */
    @Transactional
    void deleteByFileUrl(String fileUrl);
}
