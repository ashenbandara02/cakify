package com.cakify.controller;

import com.cakify.dto.ReviewRequest;
import com.cakify.dto.ReviewResponse;
import com.cakify.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ReviewController {

    private final ReviewService reviewService;

    // POST - Add a new review (public, but with buyer verification)
    @PostMapping
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse review = reviewService.addReview(productId, reviewRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    // GET - Get all reviews for a product
    @GetMapping
    public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
        try {
            List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // GET - Get review statistics (average rating and count)
    @GetMapping("/stats")
    public ResponseEntity<?> getReviewStats(@PathVariable Long productId) {
        try {
            Double averageRating = reviewService.getAverageRating(productId);
            Long reviewCount = reviewService.getReviewCount(productId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("averageRating", averageRating);
            stats.put("reviewCount", reviewCount);

            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // DELETE - Delete a review (admin only)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        try {
            boolean deleted = reviewService.deleteReview(reviewId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Review not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error deleting review");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}