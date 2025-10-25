package com.cakify.controller;

import com.cakify.dto.AnalyticsResponseDTO;
import com.cakify.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:8080")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsResponseDTO getAnalytics() {
        return analyticsService.getFullAnalytics();
    }
}
