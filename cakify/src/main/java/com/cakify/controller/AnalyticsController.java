package com.cakify.controller;

import com.cakify.dto.MonthlyRevenueDTO;
import com.cakify.dto.PopularProductDTO;
import com.cakify.dto.SummaryDTO;
import com.cakify.service.AnalyticsService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:8080")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue() {
        return ResponseEntity.ok(analyticsService.getMonthlyRevenue());
    }

    @GetMapping("/popular-products")
    public ResponseEntity<List<PopularProductDTO>> getPopularProducts() {
        return ResponseEntity.ok(analyticsService.getPopularProducts());
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
