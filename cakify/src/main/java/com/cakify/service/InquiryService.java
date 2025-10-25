package com.cakify.service;

import com.cakify.dto.InquiryRequest;
import com.cakify.dto.InquiryResponse;
import com.cakify.entity.Inquiry;
import com.cakify.entity.InquiryCategory;
import com.cakify.entity.InquiryStatus;
import com.cakify.exception.CategoryNotFoundException;
import com.cakify.exception.InquiryAlreadyResolvedException;
import com.cakify.exception.InquiryNotFoundException;
import com.cakify.exception.InquiryOperationException;
import com.cakify.exception.InquiryValidationException;
import com.cakify.repository.InquiryCategoryRepository;
import com.cakify.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * InquiryService - Core business logic for inquiry management
 * 
 * Design Patterns Implemented:
 * - Singleton Pattern: Spring @Service creates single instance
 * - Repository Pattern: Uses InquiryRepository for data access
 * - Factory Method: InquiryResponse.fromEntity() creates DTOs
 * - Bridge Pattern: Bridges Controller and Repository layers
 * - Observer Pattern: Notifies EmailService on inquiry events
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles inquiry business logic
 * - Dependency Inversion: Depends on repository interfaces
 * - Open/Closed: Extensible through inheritance
 * 
 * @author Mahima
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
    
    private final InquiryRepository inquiryRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final EmailService emailService;
    
    /**
     * Create new inquiry (customer submits)
     * Sends auto-response email to customer
     * 
     * @param request inquiry details
     * @return created inquiry response
     */
    public InquiryResponse createInquiry(InquiryRequest request) {
        validateInquiryRequest(request);
        
        try {
            Inquiry inquiry = new Inquiry();
            inquiry.setName(request.getName().trim());
            inquiry.setEmail(request.getEmail().trim().toLowerCase());
            inquiry.setMessage(request.getMessage().trim());
            inquiry.setStatus(InquiryStatus.NEW); // Always NEW when created
            
            // Set category if provided
            String categoryName = null;
            if (request.getCategoryId() != null) {
                InquiryCategory category = inquiryCategoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
                
                if (!category.getIsActive()) {
                    throw new InquiryValidationException("categoryId", "Selected category is not active");
                }
                
                inquiry.setCategory(category);
                categoryName = category.getName();
            }
            
            Inquiry savedInquiry = inquiryRepository.save(inquiry);
            
            // Send auto-response email (Observer Pattern - notify email service)
            emailService.sendInquiryAutoResponseEmail(
                savedInquiry.getEmail(),
                savedInquiry.getName(),
                savedInquiry.getId(),
                categoryName
            );
            
            return InquiryResponse.fromEntity(savedInquiry);
        } catch (CategoryNotFoundException | InquiryValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new InquiryOperationException("Failed to create inquiry", e);
        }
    }
    
    /**
     * Get all inquiries (admin view)
     * Non-paginated version for backward compatibility
     */
    public List<InquiryResponse> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all inquiries with pagination (recommended for large datasets)
     * 
     * @param page page number (0-indexed)
     * @param size items per page
     * @return paginated inquiry responses
     */
    public Page<InquiryResponse> getAllInquiriesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return inquiryRepository.findAll(pageable)
                .map(InquiryResponse::fromEntity);
    }
    
    /**
     * Get inquiries by status
     * Non-paginated version
     */
    public List<InquiryResponse> getInquiriesByStatus(String status) {
        InquiryStatus inquiryStatus = InquiryStatus.fromString(status);
        List<Inquiry> inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(inquiryStatus);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get inquiries by status with pagination
     * 
     * @param status inquiry status
     * @param page page number (0-indexed)
     * @param size items per page
     * @return paginated inquiry responses
     */
    public Page<InquiryResponse> getInquiriesByStatusPaginated(String status, int page, int size) {
        InquiryStatus inquiryStatus = InquiryStatus.fromString(status);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return inquiryRepository.findByStatus(inquiryStatus, pageable)
                .map(InquiryResponse::fromEntity);
    }
    
    // Get inquiry by ID
    public InquiryResponse getInquiryById(Long id) {
        return inquiryRepository.findById(id)
                .map(InquiryResponse::fromEntity)
                .orElseThrow(() -> new InquiryNotFoundException(id));
    }
    
    /**
     * Reply to inquiry and mark as resolved (admin action)
     * Sends email notification to customer with reply
     * 
     * @param id inquiry ID
     * @param replyMessage admin's reply
     * @return updated inquiry response
     */
    public InquiryResponse replyToInquiry(Long id, String replyMessage) {
        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            throw new InquiryValidationException("reply", "Reply message cannot be empty");
        }
        
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new InquiryNotFoundException(id));
        
        // Check if already resolved
        if (inquiry.isResolved()) {
            throw new InquiryAlreadyResolvedException(id);
        }
        
        try {
            inquiry.markAsResolved(replyMessage.trim());
            inquiry.setRepliedAt(LocalDateTime.now()); // Set reply timestamp
            Inquiry savedInquiry = inquiryRepository.save(inquiry);
            
            // Collect attachment URLs for email
            List<String> attachmentUrls = savedInquiry.getAttachments() != null ?
                savedInquiry.getAttachments().stream()
                    .map(attachment -> "/api/files/download/" + attachment.getId())
                    .collect(Collectors.toList()) :
                List.of();
            
            // Send reply email to customer (Observer Pattern)
            emailService.sendInquiryReplyEmail(
                savedInquiry.getEmail(),
                savedInquiry.getName(),
                savedInquiry.getId(),
                savedInquiry.getMessage(),
                replyMessage.trim(),
                attachmentUrls
            );
            
            return InquiryResponse.fromEntity(savedInquiry);
        } catch (Exception e) {
            throw new InquiryOperationException("Failed to reply to inquiry", e);
        }
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
    
    // Delete inquiry (admin action)
    public void deleteInquiry(Long id) {
        if (!inquiryRepository.existsById(id)) {
            throw new InquiryNotFoundException(id);
        }
        
        try {
            inquiryRepository.deleteById(id);
        } catch (Exception e) {
            throw new InquiryOperationException("Failed to delete inquiry", e);
        }
    }
    
    // Mark inquiry as new again (admin action - reopen)
    public InquiryResponse reopenInquiry(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new InquiryNotFoundException(id));
        
        try {
            inquiry.setStatus(InquiryStatus.NEW);
            inquiry.setReply(null); // Clear previous reply
            inquiry.setRepliedAt(null); // Clear reply timestamp
            Inquiry savedInquiry = inquiryRepository.save(inquiry);
            return InquiryResponse.fromEntity(savedInquiry);
        } catch (Exception e) {
            throw new InquiryOperationException("Failed to reopen inquiry", e);
        }
    }
    
    // Mark inquiry as resolved (admin action)
    public InquiryResponse resolveInquiry(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new InquiryNotFoundException(id));
        
        try {
            inquiry.setStatus(InquiryStatus.RESOLVED);
            Inquiry savedInquiry = inquiryRepository.save(inquiry);
            return InquiryResponse.fromEntity(savedInquiry);
        } catch (Exception e) {
            throw new InquiryOperationException("Failed to resolve inquiry", e);
        }
    }
    
    // Get inquiries by category
    public List<InquiryResponse> getInquiriesByCategory(Long categoryId) {
        InquiryCategory category = inquiryCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        
        List<Inquiry> inquiries = inquiryRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get inquiries with attachments
    public List<InquiryResponse> getInquiriesWithAttachments() {
        List<Inquiry> inquiries = inquiryRepository.findInquiriesWithAttachments();
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Search inquiries with category filter
    public List<InquiryResponse> searchInquiriesWithCategory(String searchTerm, Long categoryId) {
        if (categoryId != null) {
            InquiryCategory category = inquiryCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        }
        
        List<Inquiry> inquiries = inquiryRepository.searchInquiriesWithCategory(searchTerm, categoryId);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get inquiry statistics for dashboard
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalInquiries", inquiryRepository.countTotalInquiries());
        stats.put("newInquiries", inquiryRepository.countNewInquiries());
        stats.put("resolvedInquiries", inquiryRepository.countByStatus(InquiryStatus.RESOLVED));
        stats.put("inquiriesWithAttachments", inquiryRepository.countInquiriesWithAttachments());
        
        // Category-wise breakdown
        List<InquiryCategory> activeCategories = inquiryCategoryRepository.findByIsActiveTrue();
        Map<String, Long> categoryStats = new HashMap<>();
        for (InquiryCategory category : activeCategories) {
            Long count = inquiryRepository.countByCategoryId(category.getId());
            categoryStats.put(category.getName(), count);
        }
        stats.put("categoryCounts", categoryStats);
        
        // Recent inquiries (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        stats.put("recentInquiries", inquiryRepository.countByCreatedAtAfter(weekAgo));
        
        return stats;
    }
    
    // Get inquiries by category and status
    public List<InquiryResponse> getInquiriesByCategoryAndStatus(Long categoryId, String status) {
        InquiryCategory category = inquiryCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        
        InquiryStatus inquiryStatus = InquiryStatus.fromString(status);
        List<Inquiry> inquiries = inquiryRepository.findByCategoryIdAndStatus(categoryId, inquiryStatus);
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Get unanswered inquiries
    public List<InquiryResponse> getUnansweredInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findUnansweredInquiries();
        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Validation helper
    private void validateInquiryRequest(InquiryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InquiryValidationException("name", "Name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InquiryValidationException("email", "Email is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().length() < 10) {
            throw new InquiryValidationException("message", "Message must be at least 10 characters long");
        }
        if (request.getName().length() > 100) {
            throw new InquiryValidationException("name", "Name must be less than 100 characters");
        }
        if (request.getEmail().length() > 100) {
            throw new InquiryValidationException("email", "Email must be less than 100 characters");
        }
    }
    
    // ============== ANALYTICS METHODS ==============
    
    /**
     * Get chart data for inquiries per month (last 12 months)
     * Returns data suitable for line/bar charts
     * 
     * Design Pattern: Strategy Pattern - Different time period strategies
     */
    public Map<String, Object> getInquiriesPerMonth() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        List<Object[]> monthlyData = inquiryRepository.getInquiriesPerMonth(twelveMonthsAgo);
        
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] row : monthlyData) {
            labels.add((String) row[0]); // Year-Month (e.g., "2024-10")
            counts.add((Long) row[1]);   // Count
        }
        
        chartData.put("labels", labels);
        chartData.put("data", counts);
        chartData.put("title", "Inquiries per Month (Last 12 Months)");
        
        return chartData;
    }
    
    /**
     * Get chart data for inquiries per week (last 8 weeks)
     */
    public Map<String, Object> getInquiriesPerWeek() {
        LocalDateTime eightWeeksAgo = LocalDateTime.now().minusWeeks(8);
        List<Object[]> weeklyData = inquiryRepository.getInquiriesPerWeek(eightWeeksAgo);
        
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] row : weeklyData) {
            labels.add("Week " + row[0]); // Week number
            counts.add((Long) row[1]);     // Count
        }
        
        chartData.put("labels", labels);
        chartData.put("data", counts);
        chartData.put("title", "Inquiries per Week (Last 8 Weeks)");
        
        return chartData;
    }
    
    /**
     * Get chart data for inquiries per day (last 30 days)
     */
    public Map<String, Object> getInquiriesPerDay() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyData = inquiryRepository.getInquiriesPerDay(thirtyDaysAgo);
        
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] row : dailyData) {
            labels.add((String) row[0]); // Date (YYYY-MM-DD)
            counts.add((Long) row[1]);   // Count
        }
        
        chartData.put("labels", labels);
        chartData.put("data", counts);
        chartData.put("title", "Inquiries per Day (Last 30 Days)");
        
        return chartData;
    }
    
    /**
     * Get category distribution for pie/donut charts
     * Returns percentage breakdown by category
     */
    public Map<String, Object> getCategoryDistribution() {
        List<Object[]> distribution = inquiryRepository.getCategoryDistribution();
        
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<Double> percentages = new ArrayList<>();
        
        for (Object[] row : distribution) {
            String categoryName = row[0] != null ? (String) row[0] : "Uncategorized";
            labels.add(categoryName);
            counts.add((Long) row[1]);
            percentages.add((Double) row[2]);
        }
        
        chartData.put("labels", labels);
        chartData.put("data", counts);
        chartData.put("percentages", percentages);
        chartData.put("title", "Inquiries by Category");
        
        return chartData;
    }
    
    /**
     * Get status distribution for pie charts
     */
    public Map<String, Object> getStatusDistribution() {
        List<Object[]> distribution = inquiryRepository.getStatusDistribution();
        
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] row : distribution) {
            labels.add(row[0].toString()); // Status name
            counts.add((Long) row[1]);     // Count
        }
        
        chartData.put("labels", labels);
        chartData.put("data", counts);
        chartData.put("title", "Inquiries by Status");
        
        return chartData;
    }
    
    /**
     * Get response time metrics for performance dashboard
     * Returns comprehensive response time analytics
     */
    public Map<String, Object> getResponseTimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
    // Average response time (repo returns seconds)
    Double avgResponseTimeSec = inquiryRepository.getAverageResponseTimeInHours();
    metrics.put("averageResponseTimeHours", avgResponseTimeSec != null ? (avgResponseTimeSec / 3600.0) : 0.0);
        
    // Median response time (repo returns seconds)
    Double medianResponseTimeSec = inquiryRepository.getMedianResponseTimeInHours();
    metrics.put("medianResponseTimeHours", medianResponseTimeSec != null ? (medianResponseTimeSec / 3600.0) : 0.0);
        
        // Response time range (min, max) in seconds -> convert to hours here
        Object[] range = inquiryRepository.getResponseTimeRange();
        if (range != null && range.length == 2) {
            Double minSec = range[0] instanceof Number ? ((Number) range[0]).doubleValue() : 0.0;
            Double maxSec = range[1] instanceof Number ? ((Number) range[1]).doubleValue() : 0.0;
            metrics.put("fastestResponseHours", minSec / 3600.0);
            metrics.put("slowestResponseHours", maxSec / 3600.0);
        }
        
        // Response time by category (repo returns seconds)
        List<Object[]> categoryResponseTimes = inquiryRepository.getAverageResponseTimeByCategory();
        Map<String, Double> categoryMetrics = new HashMap<>();
        for (Object[] row : categoryResponseTimes) {
            String cat = (String) row[0];
            Double sec = row[1] instanceof Number ? ((Number) row[1]).doubleValue() : 0.0;
            categoryMetrics.put(cat, sec / 3600.0);
        }
        metrics.put("responseTimeByCategory", categoryMetrics);
        
        // Overdue inquiries (pending > 24 hours)
    long overdueCount = inquiryRepository.countOverdueInquiries(24 * 3600);
        metrics.put("overdueInquiries", overdueCount);
        
        // SLA compliance (assuming 24-hour SLA)
        long totalResolved = inquiryRepository.countByStatus(InquiryStatus.RESOLVED);
        long within24Hours = totalResolved - inquiryRepository.getSlowResponseInquiries().size();
        double slaCompliance = totalResolved > 0 ? (within24Hours * 100.0 / totalResolved) : 100.0;
        metrics.put("slaCompliancePercentage", slaCompliance);
        
        return metrics;
    }
    
    /**
     * Get trend analysis comparing current period with previous
     * Shows growth/decline patterns
     */
    public Map<String, Object> getTrendAnalysis() {
        Map<String, Object> trends = new HashMap<>();
        
        // Monthly trend
        List<Object> monthlyTrend = inquiryRepository.getMonthlyTrend();
        if (monthlyTrend != null && monthlyTrend.size() == 3) {
            trends.put("currentMonthCount", monthlyTrend.get(0));
            trends.put("previousMonthCount", monthlyTrend.get(1));
            trends.put("monthlyChangePercentage", monthlyTrend.get(2));
        }
        
        // Weekly comparison
        LocalDateTime thisWeekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime lastWeekStart = LocalDateTime.now().minusDays(14);
        long thisWeekCount = inquiryRepository.countByCreatedAtAfter(thisWeekStart);
        long lastWeekCount = inquiryRepository.countByCreatedAtAfter(lastWeekStart) - thisWeekCount;
        double weeklyChange = lastWeekCount > 0 ? ((thisWeekCount - lastWeekCount) * 100.0 / lastWeekCount) : 0.0;
        
        trends.put("thisWeekCount", thisWeekCount);
        trends.put("lastWeekCount", lastWeekCount);
        trends.put("weeklyChangePercentage", weeklyChange);
        
        // Daily average
        long last30DaysCount = inquiryRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30));
        double dailyAverage = last30DaysCount / 30.0;
        trends.put("dailyAverageInquiries", dailyAverage);
        
        return trends;
    }
    
    /**
     * Get operational analytics for staffing optimization
     * Shows busiest hours and days
     */
    public Map<String, Object> getOperationalAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Busiest hours
        List<Object[]> busiestHours = inquiryRepository.getBusiestHours();
        Map<String, Long> hourlyDistribution = new HashMap<>();
        for (Object[] row : busiestHours) {
            int hour = ((Number) row[0]).intValue();
            String hourLabel = String.format("%02d:00 - %02d:00", hour, (hour + 1) % 24);
            hourlyDistribution.put(hourLabel, (Long) row[1]);
        }
        analytics.put("busiestHours", hourlyDistribution);
        
        // Busiest days of week
        List<Object[]> busiestDays = inquiryRepository.getBusiestDaysOfWeek();
        Map<String, Long> dayDistribution = new HashMap<>();
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (Object[] row : busiestDays) {
            int dayOfWeek = ((Number) row[0]).intValue();
            dayDistribution.put(dayNames[dayOfWeek], (Long) row[1]);
        }
        analytics.put("busiestDays", dayDistribution);
        
        return analytics;
    }
    
    /**
     * Get comprehensive performance analytics dashboard
     * Combines all analytics for admin overview
     * 
     * Design Pattern: Facade Pattern - Simplified interface to complex analytics
     */
    public Map<String, Object> getPerformanceAnalytics() {
        Map<String, Object> performanceData = new HashMap<>();
        
        // Basic statistics
        performanceData.put("basicStats", getDashboardStatistics());
        
        // Time-based trends
        performanceData.put("monthlyTrend", getInquiriesPerMonth());
        performanceData.put("weeklyTrend", getInquiriesPerWeek());
        performanceData.put("dailyTrend", getInquiriesPerDay());
        
        // Distribution analysis
        performanceData.put("categoryDistribution", getCategoryDistribution());
        performanceData.put("statusDistribution", getStatusDistribution());
        
        // Response metrics
        performanceData.put("responseMetrics", getResponseTimeMetrics());
        
        // Trend analysis
        performanceData.put("trendAnalysis", getTrendAnalysis());
        
        // Operational insights
        performanceData.put("operationalAnalytics", getOperationalAnalytics());
        
        // Metadata
        performanceData.put("generatedAt", LocalDateTime.now());
        performanceData.put("dataRange", "Last 12 months");
        
        return performanceData;
    }
}