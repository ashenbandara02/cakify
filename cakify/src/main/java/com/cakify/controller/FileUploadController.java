package com.cakify.controller;

import com.cakify.dto.AttachmentResponse;
import com.cakify.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for file upload operations
 * Demonstrates:
 * - Strategy Pattern (file validation strategies)
 * - Factory Method Pattern (AttachmentResponse.fromEntity)
 * - Singleton Pattern (Spring-managed bean)
 */
@RestController
@RequestMapping("/api/inquiries/{inquiryId}/attachments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    /**
     * Upload single file for inquiry
     * POST /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId inquiry ID
     * @param file file to upload
     * @return uploaded attachment details
     */
    @PostMapping
    public ResponseEntity<AttachmentResponse> uploadFile(
            @PathVariable Long inquiryId,
            @RequestParam("file") MultipartFile file) {
        AttachmentResponse response = fileUploadService.uploadFile(file, inquiryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Upload multiple files for inquiry
     * POST /api/inquiries/{inquiryId}/attachments/multiple
     * 
     * @param inquiryId inquiry ID
     * @param files files to upload
     * @return list of uploaded attachments
     */
    @PostMapping("/multiple")
    public ResponseEntity<List<AttachmentResponse>> uploadMultipleFiles(
            @PathVariable Long inquiryId,
            @RequestParam("files") MultipartFile[] files) {
        List<AttachmentResponse> responses = fileUploadService.uploadMultipleFiles(
                List.of(files), inquiryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    /**
     * Get all attachments for inquiry
     * GET /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId inquiry ID
     * @return list of attachments
     */
    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByInquiry(@PathVariable Long inquiryId) {
        List<AttachmentResponse> attachments = fileUploadService.getAttachmentsByInquiryId(inquiryId);
        return ResponseEntity.ok(attachments);
    }
    
    /**
     * Get attachment by ID
     * GET /api/inquiries/{inquiryId}/attachments/{attachmentId}
     * 
     * @param inquiryId inquiry ID
     * @param attachmentId attachment ID
     * @return attachment details
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponse> getAttachment(
            @PathVariable Long inquiryId,
            @PathVariable Long attachmentId) {
        AttachmentResponse attachment = fileUploadService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(attachment);
    }
    
    /**
     * Get attachment metadata for download
     * GET /api/inquiries/{inquiryId}/attachments/{attachmentId}/download
     * 
     * @param inquiryId inquiry ID
     * @param attachmentId attachment ID
     * @return attachment metadata with download URL
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<AttachmentResponse> getDownloadInfo(
            @PathVariable Long inquiryId,
            @PathVariable Long attachmentId) {
        
        AttachmentResponse attachment = fileUploadService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(attachment);
    }
    
    /**
     * Delete attachment
     * DELETE /api/inquiries/{inquiryId}/attachments/{attachmentId}
     * 
     * @param inquiryId inquiry ID
     * @param attachmentId attachment ID
     * @return no content
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long inquiryId,
            @PathVariable Long attachmentId) {
        fileUploadService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete all attachments for inquiry
     * DELETE /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId inquiry ID
     * @return no content
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllAttachments(@PathVariable Long inquiryId) {
        List<AttachmentResponse> attachments = fileUploadService.getAttachmentsByInquiryId(inquiryId);
        attachments.forEach(att -> fileUploadService.deleteAttachment(att.getId()));
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get total storage used by inquiry
     * GET /api/inquiries/{inquiryId}/attachments/storage
     * 
     * @param inquiryId inquiry ID
     * @return storage size in bytes
     */
    @GetMapping("/storage")
    public ResponseEntity<Long> getTotalStorageUsed(@PathVariable Long inquiryId) {
        List<AttachmentResponse> attachments = fileUploadService.getAttachmentsByInquiryId(inquiryId);
        long totalBytes = attachments.stream()
                .mapToLong(AttachmentResponse::getFileSize)
                .sum();
        return ResponseEntity.ok(totalBytes);
    }
}
