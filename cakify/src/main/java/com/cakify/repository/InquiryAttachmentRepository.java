package com.cakify.repository;

import com.cakify.entity.InquiryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * InquiryAttachmentRepository - Data access layer for InquiryAttachment entity
 * 
 * Design Patterns:
 * - Repository Pattern: Abstracts data access logic
 * - Singleton Pattern: Spring creates single instance
 */
@Repository
public interface InquiryAttachmentRepository extends JpaRepository<InquiryAttachment, Long> {
    
    /**
     * Find all attachments for a specific inquiry
     * @param inquiryId the inquiry ID
     * @return list of attachments
     */
    List<InquiryAttachment> findByInquiryId(Long inquiryId);
    
    /**
     * Delete all attachments for a specific inquiry
     * @param inquiryId the inquiry ID
     */
    @Modifying
    @Transactional
    void deleteByInquiryId(Long inquiryId);
    
    /**
     * Count attachments for a specific inquiry
     * @param inquiryId the inquiry ID
     * @return number of attachments
     */
    long countByInquiryId(Long inquiryId);
    
    /**
     * Calculate total storage used by all attachments
     * @return total file size in bytes
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM InquiryAttachment a")
    long calculateTotalStorageUsed();
    
    /**
     * Calculate storage used by a specific inquiry
     * @param inquiryId the inquiry ID
     * @return total file size in bytes for this inquiry
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM InquiryAttachment a WHERE a.inquiry.id = :inquiryId")
    long calculateStorageByInquiry(@Param("inquiryId") Long inquiryId);
    
    /**
     * Find attachments by file type
     * @param fileType the MIME type (e.g., "image/png")
     * @return list of attachments
     */
    List<InquiryAttachment> findByFileType(String fileType);
    
    /**
     * Find all image attachments
     * @return list of image attachments
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.fileType LIKE 'image/%'")
    List<InquiryAttachment> findAllImages();
    
    /**
     * Find all PDF attachments
     * @return list of PDF attachments
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.fileType = 'application/pdf'")
    List<InquiryAttachment> findAllPdfs();
    
    /**
     * Find attachments larger than specified size
     * @param sizeInBytes minimum file size
     * @return list of large attachments
     */
    @Query("SELECT a FROM InquiryAttachment a WHERE a.fileSize > :size")
    List<InquiryAttachment> findAttachmentsLargerThan(@Param("size") long sizeInBytes);
}
