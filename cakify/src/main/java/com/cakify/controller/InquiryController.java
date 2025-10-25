package com.cakify.controller;

import com.cakify.dto.InquiryRequest;
import com.cakify.dto.InquiryResponse;
import com.cakify.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Inquiry Management
 * 
 * Security Notes (for integration with Ashen's security module):
 * - Public endpoints: createInquiry, getInquiryById (customer access)
 * - Admin-only: getAllInquiries, reply, delete, reopen, stats, search, pagination
 * - Use @PreAuthorize("hasRole('ADMIN')") when security module is integrated
 */

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080") // Allow frontend access
public class InquiryController {
    
    private final InquiryService inquiryService;
    
    // POST /api/inquiries - Customer submits inquiry (Public endpoint)
    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResponse createdInquiry = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInquiry);
    }
    
    // GET /api/inquiries - Admin gets all inquiries
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getAllInquiries(
            @RequestParam(value = "status", required = false) String status) {
        
        List<InquiryResponse> inquiries;
        
        if (status != null && !status.trim().isEmpty()) {
            inquiries = inquiryService.getInquiriesByStatus(status);
        } else {
            inquiries = inquiryService.getAllInquiries();
        }
        
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/paginated - Admin gets inquiries with pagination
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @GetMapping("/paginated")
    public ResponseEntity<Page<InquiryResponse>> getInquiriesPaginated(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status) {
        
        Page<InquiryResponse> inquiries;
        
        if (status != null && !status.trim().isEmpty()) {
            inquiries = inquiryService.getInquiriesByStatusPaginated(status, page, size);
        } else {
            inquiries = inquiryService.getAllInquiriesPaginated(page, size);
        }
        
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/{id} - Get specific inquiry by ID
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiryById(@PathVariable Long id) {
        InquiryResponse inquiry = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(inquiry);
    }
    
    // PUT /api/inquiries/{id}/reply - Admin replies to inquiry
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @PutMapping("/{id}/reply")
    public ResponseEntity<InquiryResponse> replyToInquiry(
            @PathVariable Long id, 
            @RequestBody Map<String, String> replyData) {
        
        String replyMessage = replyData.get("reply");
        InquiryResponse updatedInquiry = inquiryService.replyToInquiry(id, replyMessage);
        return ResponseEntity.ok(updatedInquiry);
    }
    
    // GET /api/inquiries/search?q={searchTerm} - Search inquiries
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @GetMapping("/search")
    public ResponseEntity<List<InquiryResponse>> searchInquiries(
            @RequestParam("q") String searchTerm) {
        List<InquiryResponse> inquiries = inquiryService.searchInquiries(searchTerm);
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/stats - Get inquiry statistics (for admin dashboard)
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInquiryStats() {
        Map<String, Object> stats = inquiryService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }
    
    // GET /api/inquiries/customer/{email} - Get inquiries by customer email
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<InquiryResponse>> getInquiriesByEmail(@PathVariable String email) {
        List<InquiryResponse> inquiries = inquiryService.getInquiriesByEmail(email);
        return ResponseEntity.ok(inquiries);
    }
    
    // DELETE /api/inquiries/{id} - Delete inquiry (Admin only)
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.noContent().build();
    }
    
    // PUT /api/inquiries/{id}/reopen - Reopen resolved inquiry (Admin only)
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @PutMapping("/{id}/reopen")
    public ResponseEntity<InquiryResponse> reopenInquiry(@PathVariable Long id) {
        InquiryResponse reopenedInquiry = inquiryService.reopenInquiry(id);
        return ResponseEntity.ok(reopenedInquiry);
    }
    
    // GET /api/inquiries/new - Get only new inquiries (shortcut for admin)
    @GetMapping("/new")
    public ResponseEntity<List<InquiryResponse>> getNewInquiries() {
        List<InquiryResponse> newInquiries = inquiryService.getInquiriesByStatus("new");
        return ResponseEntity.ok(newInquiries);
    }
    
    // GET /api/inquiries/resolved - Get only resolved inquiries (shortcut for admin)
    @GetMapping("/resolved")
    public ResponseEntity<List<InquiryResponse>> getResolvedInquiries() {
        List<InquiryResponse> resolvedInquiries = inquiryService.getInquiriesByStatus("resolved");
        return ResponseEntity.ok(resolvedInquiries);
    }
    
    // ============== NEW: Category-based endpoints ==============
    
    // GET /api/inquiries/category/{categoryId} - Get inquiries by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<InquiryResponse>> getInquiriesByCategory(@PathVariable Long categoryId) {
        List<InquiryResponse> inquiries = inquiryService.getInquiriesByCategory(categoryId);
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/category/{categoryId}/status/{status} - Get inquiries by category and status
    @GetMapping("/category/{categoryId}/status/{status}")
    public ResponseEntity<List<InquiryResponse>> getInquiriesByCategoryAndStatus(
            @PathVariable Long categoryId,
            @PathVariable String status) {
        List<InquiryResponse> inquiries = inquiryService.getInquiriesByCategoryAndStatus(categoryId, status);
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/with-files - Get inquiries with attachments
    @GetMapping("/with-files")
    public ResponseEntity<List<InquiryResponse>> getInquiriesWithFiles() {
        List<InquiryResponse> inquiries = inquiryService.getInquiriesWithAttachments();
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/unanswered - Get unanswered inquiries
    @GetMapping("/unanswered")
    public ResponseEntity<List<InquiryResponse>> getUnansweredInquiries() {
        List<InquiryResponse> inquiries = inquiryService.getUnansweredInquiries();
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/search/advanced - Advanced search with category filter
    @GetMapping("/search/advanced")
    public ResponseEntity<List<InquiryResponse>> searchInquiriesAdvanced(
            @RequestParam("q") String searchTerm,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        List<InquiryResponse> inquiries = inquiryService.searchInquiriesWithCategory(searchTerm, categoryId);
        return ResponseEntity.ok(inquiries);
    }
    
    // PUT /api/inquiries/{id}/resolve - Mark inquiry as resolved (Admin only)
    // TODO: Add @PreAuthorize("hasRole('ADMIN')") when security module is ready
    @PostMapping("/{id}/resolve")
    public ResponseEntity<InquiryResponse> resolveInquiry(@PathVariable Long id) {
        InquiryResponse resolvedInquiry = inquiryService.resolveInquiry(id);
        return ResponseEntity.ok(resolvedInquiry);
    }
}