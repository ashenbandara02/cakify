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
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequest reviewRequest) {
        ReviewResponse review = reviewService.addReview(productId, reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // GET - Get all reviews for a product
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    // GET - Get review statistics (average rating and count)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRating(productId);
        Long reviewCount = reviewService.getReviewCount(productId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", averageRating);
        stats.put("reviewCount", reviewCount);

        return ResponseEntity.ok(stats);
    }

    // GET - Get all reviews for admin (including unapproved)
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponse>> getAllProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    // PATCH - Update review approval status (admin only)
    @PatchMapping("/{reviewId}/approve")
    public ResponseEntity<ReviewResponse> updateReviewApproval(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestParam Boolean approved) {
        ReviewResponse updatedReview = reviewService.updateReviewApproval(productId, reviewId, approved);
        return ResponseEntity.ok(updatedReview);
    }

    // DELETE - Delete a review (admin only)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}