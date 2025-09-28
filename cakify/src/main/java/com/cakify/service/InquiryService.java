package com.cakify.service;

import com.cakify.dto.InquiryRequest;
import com.cakify.dto.InquiryResponse;
import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryStatus;
import com.cakify.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
    
    private final InquiryRepository inquiryRepository;
    
    // Create new inquiry (customer submits)
    public InquiryResponse createInquiry(InquiryRequest request) {
        validateInquiryRequest(request);
        
        Inquiry inquiry = new Inquiry();
        inquiry.setName(request.getName().trim());
        inquiry.setEmail(request.getEmail().trim().toLowerCase());
        inquiry.setMessage(request.getMessage().trim());
        inquiry.setStatus(InquiryStatus.NEW); // Always NEW when created
        
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return InquiryResponse.fromEntity(savedInquiry);
    }
    
    // Get all inquiries (admin view)
    public List<InquiryResponse> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get inquiries by status
    public List<InquiryResponse> getInquiriesByStatus(String status) {
        InquiryStatus inquiryStatus = InquiryStatus.fromString(status);
        List<Inquiry> inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(inquiryStatus);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get inquiry by ID
    public Optional<InquiryResponse> getInquiryById(Long id) {
        return inquiryRepository.findById(id)
                .map(InquiryResponse::fromEntity);
    }
    
    // Reply to inquiry and mark as resolved (admin action)
    public Optional<InquiryResponse> replyToInquiry(Long id, String replyMessage) {
        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply message cannot be empty");
        }
        
        return inquiryRepository.findById(id)
                .map(inquiry -> {
                    inquiry.markAsResolved(replyMessage.trim());
                    Inquiry savedInquiry = inquiryRepository.save(inquiry);
                    return InquiryResponse.fromEntity(savedInquiry);
                });
    }
    
    // Search inquiries
    public List<InquiryResponse> searchInquiries(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllInquiries();
        }
        
        List<Inquiry> inquiries = inquiryRepository.searchInquiries(searchTerm.trim());
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get new inquiries count (for dashboard)
    public long getNewInquiriesCount() {
        return inquiryRepository.countNewInquiries();
    }
    
    // Get total inquiries count
    public long getTotalInquiriesCount() {
        return inquiryRepository.countTotalInquiries();
    }
    
    // Get inquiries by customer email
    public List<InquiryResponse> getInquiriesByEmail(String email) {
        List<Inquiry> inquiries = inquiryRepository.findByEmailIgnoreCaseOrderByCreatedAtDesc(email);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Delete inquiry (admin action - soft delete alternative)
    public boolean deleteInquiry(Long id) {
        if (inquiryRepository.existsById(id)) {
            inquiryRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Mark inquiry as new again (admin action - reopen)
    public Optional<InquiryResponse> reopenInquiry(Long id) {
        return inquiryRepository.findById(id)
                .map(inquiry -> {
                    inquiry.setStatus(InquiryStatus.NEW);
                    inquiry.setReply(null); // Clear previous reply
                    Inquiry savedInquiry = inquiryRepository.save(inquiry);
                    return InquiryResponse.fromEntity(savedInquiry);
                });
    }
    
    // Validation helper
    private void validateInquiryRequest(InquiryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().length() < 10) {
            throw new IllegalArgumentException("Message must be at least 10 characters long");
        }
        if (request.getName().length() > 100) {
            throw new IllegalArgumentException("Name must be less than 100 characters");
        }
        if (request.getEmail().length() > 100) {
            throw new IllegalArgumentException("Email must be less than 100 characters");
        }
    }
}