package com.cakify.controller;

import com.cakify.dto.InquiryRequest;
import com.cakify.dto.InquiryResponse;
import com.cakify.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend access
public class InquiryController {
    
    private final InquiryService inquiryService;
    
    // POST /api/inquiries - Customer submits inquiry (Public endpoint)
    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(@Valid @RequestBody InquiryRequest request) {
        try {
            InquiryResponse createdInquiry = inquiryService.createInquiry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInquiry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/inquiries - Admin gets all inquiries
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
    
    // GET /api/inquiries/{id} - Get specific inquiry by ID
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiryById(@PathVariable Long id) {
        Optional<InquiryResponse> inquiry = inquiryService.getInquiryById(id);
        return inquiry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // PUT /api/inquiries/{id}/reply - Admin replies to inquiry
    @PutMapping("/{id}/reply")
    public ResponseEntity<InquiryResponse> replyToInquiry(
            @PathVariable Long id, 
            @RequestBody Map<String, String> replyData) {
        
        String replyMessage = replyData.get("reply");
        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<InquiryResponse> updatedInquiry = inquiryService.replyToInquiry(id, replyMessage);
            return updatedInquiry.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/inquiries/search?q={searchTerm} - Search inquiries
    @GetMapping("/search")
    public ResponseEntity<List<InquiryResponse>> searchInquiries(
            @RequestParam("q") String searchTerm) {
        List<InquiryResponse> inquiries = inquiryService.searchInquiries(searchTerm);
        return ResponseEntity.ok(inquiries);
    }
    
    // GET /api/inquiries/stats - Get inquiry statistics (for admin dashboard)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getInquiryStats() {
        Map<String, Long> stats = Map.of(
                "total", inquiryService.getTotalInquiriesCount(),
                "new", inquiryService.getNewInquiriesCount(),
                "resolved", inquiryService.getTotalInquiriesCount() - inquiryService.getNewInquiriesCount()
        );
        return ResponseEntity.ok(stats);
    }
    
    // GET /api/inquiries/customer/{email} - Get inquiries by customer email
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<InquiryResponse>> getInquiriesByEmail(@PathVariable String email) {
        List<InquiryResponse> inquiries = inquiryService.getInquiriesByEmail(email);
        return ResponseEntity.ok(inquiries);
    }
    
    // DELETE /api/inquiries/{id} - Delete inquiry (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long id) {
        boolean deleted = inquiryService.deleteInquiry(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // PUT /api/inquiries/{id}/reopen - Reopen resolved inquiry (Admin only)
    @PutMapping("/{id}/reopen")
    public ResponseEntity<InquiryResponse> reopenInquiry(@PathVariable Long id) {
        Optional<InquiryResponse> reopenedInquiry = inquiryService.reopenInquiry(id);
        return reopenedInquiry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
}