package com.cakify.controller;

import com.cakify.dto.InquiryAttachmentResponse;
import com.cakify.service.InquiryAttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InquiryAttachmentController {
    
    private final InquiryAttachmentService attachmentService;
    
    /**
     * Upload attachment to an inquiry
     * POST /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId The inquiry ID
     * @param file The file to upload
     * @return InquiryAttachmentResponse with 201 Created
     */
    @PostMapping("/{inquiryId}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long inquiryId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            InquiryAttachmentResponse response = attachmentService.uploadAttachment(inquiryId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload attachment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get all attachments for an inquiry
     * GET /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId The inquiry ID
     * @return List of attachments
     */
    @GetMapping("/{inquiryId}/attachments")
    public ResponseEntity<List<InquiryAttachmentResponse>> getAttachmentsByInquiry(
            @PathVariable Long inquiryId) {
        
        try {
            List<InquiryAttachmentResponse> attachments = 
                attachmentService.getAttachmentsByInquiryId(inquiryId);
            return ResponseEntity.ok(attachments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific attachment by ID
     * GET /api/inquiries/{inquiryId}/attachments/{attachmentId}
     * 
     * @param inquiryId The inquiry ID
     * @param attachmentId The attachment ID
     * @return InquiryAttachmentResponse
     */
    @GetMapping("/{inquiryId}/attachments/{attachmentId}")
    public ResponseEntity<InquiryAttachmentResponse> getAttachmentById(
            @PathVariable Long inquiryId,
            @PathVariable Long attachmentId) {
        
        return attachmentService.getAttachmentById(attachmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get image attachments for an inquiry
     * GET /api/inquiries/{inquiryId}/attachments/images
     * 
     * @param inquiryId The inquiry ID
     * @return List of image attachments
     */
    @GetMapping("/{inquiryId}/attachments/images")
    public ResponseEntity<List<InquiryAttachmentResponse>> getImageAttachments(
            @PathVariable Long inquiryId) {
        
        List<InquiryAttachmentResponse> images = 
            attachmentService.getImageAttachments(inquiryId);
        return ResponseEntity.ok(images);
    }
    
    /**
     * Get document attachments for an inquiry
     * GET /api/inquiries/{inquiryId}/attachments/documents
     * 
     * @param inquiryId The inquiry ID
     * @return List of document attachments
     */
    @GetMapping("/{inquiryId}/attachments/documents")
    public ResponseEntity<List<InquiryAttachmentResponse>> getDocumentAttachments(
            @PathVariable Long inquiryId) {
        
        List<InquiryAttachmentResponse> documents = 
            attachmentService.getDocumentAttachments(inquiryId);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Download/serve attachment file
     * GET /api/inquiries/attachments/download/{fileName}
     * 
     * @param fileName The file name
     * @param request HttpServletRequest for content type detection
     * @return File as Resource
     */
    @GetMapping("/attachments/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String fileName,
            HttpServletRequest request) {
        
        try {
            // Load file as Resource
            Resource resource = attachmentService.loadAttachmentAsResource(fileName);
            
            // Determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext()
                        .getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                System.out.println("Could not determine file type for: " + fileName);
            }
            
            // Fallback to default content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Determine if file should be displayed inline or downloaded
            boolean isImage = contentType.startsWith("image/");
            boolean isPdf = contentType.equals("application/pdf");
            
            String disposition = (isImage || isPdf) ? "inline" : "attachment";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            disposition + "; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("❌ Error serving attachment: " + fileName + " - " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a specific attachment
     * DELETE /api/inquiries/{inquiryId}/attachments/{attachmentId}
     * 
     * @param inquiryId The inquiry ID
     * @param attachmentId The attachment ID
     * @return 204 No Content if successful, 404 if not found
     */
    @DeleteMapping("/{inquiryId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long inquiryId,
            @PathVariable Long attachmentId) {
        
        try {
            boolean deleted = attachmentService.deleteAttachment(attachmentId);
            return deleted ? 
                ResponseEntity.noContent().build() : 
                ResponseEntity.notFound().build();
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete all attachments for an inquiry
     * DELETE /api/inquiries/{inquiryId}/attachments
     * 
     * @param inquiryId The inquiry ID
     * @return Map with count of deleted attachments
     */
    @DeleteMapping("/{inquiryId}/attachments")
    public ResponseEntity<Map<String, Object>> deleteAllAttachments(
            @PathVariable Long inquiryId) {
        
        try {
            int deletedCount = attachmentService.deleteAllAttachmentsByInquiryId(inquiryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attachments deleted successfully");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete attachments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get attachment statistics for an inquiry
     * GET /api/inquiries/{inquiryId}/attachments/stats
     * 
     * @param inquiryId The inquiry ID
     * @return Statistics including count and total size
     */
    @GetMapping("/{inquiryId}/attachments/stats")
    public ResponseEntity<Map<String, Object>> getAttachmentStats(
            @PathVariable Long inquiryId) {
        
        try {
            long count = attachmentService.getAttachmentCount(inquiryId);
            Long totalSize = attachmentService.getTotalStorageByInquiry(inquiryId);
            boolean hasAttachments = attachmentService.hasAttachments(inquiryId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("inquiryId", inquiryId);
            stats.put("attachmentCount", count);
            stats.put("totalSize", totalSize);
            stats.put("totalSizeFormatted", formatFileSize(totalSize));
            stats.put("hasAttachments", hasAttachments);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get global attachment statistics
     * GET /api/inquiries/attachments/stats/global
     * 
     * @return Global statistics
     */
    @GetMapping("/attachments/stats/global")
    public ResponseEntity<Map<String, Object>> getGlobalAttachmentStats() {
        
        try {
            Long totalStorage = attachmentService.getTotalStorageUsed();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStorageUsed", totalStorage);
            stats.put("totalStorageFormatted", formatFileSize(totalStorage));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if inquiry has attachments
     * GET /api/inquiries/{inquiryId}/attachments/exists
     * 
     * @param inquiryId The inquiry ID
     * @return Boolean response
     */
    @GetMapping("/{inquiryId}/attachments/exists")
    public ResponseEntity<Map<String, Boolean>> checkAttachmentsExist(
            @PathVariable Long inquiryId) {
        
        boolean exists = attachmentService.hasAttachments(inquiryId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasAttachments", exists);
        
        return ResponseEntity.ok(response);
    }
    
    // ===== Helper Methods =====
    
    /**
     * Format file size to human-readable format
     * @param size Size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        
        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }
}
