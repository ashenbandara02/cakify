package com.cakify.service;

import com.cakify.dto.ReviewRequest;
import com.cakify.dto.ReviewResponse;
import com.cakify.entity.Product;
import com.cakify.entity.Review;
import com.cakify.exception.DuplicateReviewException;
import com.cakify.exception.ProductNotFoundException;
import com.cakify.exception.ReviewNotFoundException;
import com.cakify.exception.ReviewValidationException;
import com.cakify.exception.UnverifiedBuyerException;
import com.cakify.repository.OrderRepository;
import com.cakify.repository.ProductRepository;
import com.cakify.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // Check if customer is a verified buyer (has completed order)
    public boolean isVerifiedBuyer(String email, Long productId) {
        return orderRepository.existsByEmailAndProductIdAndStatus(email, productId);
    }

    // Add review (with verification)
    public ReviewResponse addReview(Long productId, ReviewRequest reviewRequest) {
        // Validate review request
        validateReviewRequest(reviewRequest);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Verify buyer
        if (!isVerifiedBuyer(reviewRequest.getEmail(), productId)) {
            throw new UnverifiedBuyerException("Only verified buyers can leave reviews. You must have a completed order for this product.");
        }

        // Check if user already reviewed this product
        if (reviewRepository.findByProductIdAndEmail(productId, reviewRequest.getEmail()).isPresent()) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }

        // Create and save review
        Review review = new Review();
        review.setProduct(product);
        review.setEmail(reviewRequest.getEmail());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.fromEntity(savedReview);
    }

    // Get all reviews for a product
    public List<ReviewResponse> getProductReviews(Long productId) {
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get average rating for a product
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId)
                .orElse(0.0);
    }

    // Get review count for a product
    public Long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    // Get review by ID
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));
        return ReviewResponse.fromEntity(review);
    }

    // Delete review (admin only)
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException("Review not found with ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    // Helper method to validate review request
    private void validateReviewRequest(ReviewRequest reviewRequest) {
        if (reviewRequest.getEmail() == null || reviewRequest.getEmail().trim().isEmpty()) {
            throw new ReviewValidationException("Email is required");
        }
        if (reviewRequest.getRating() == null || reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            throw new ReviewValidationException("Rating must be between 1 and 5");
        }
        if (reviewRequest.getComment() == null || reviewRequest.getComment().trim().isEmpty()) {
            throw new ReviewValidationException("Comment is required");
        }
        if (reviewRequest.getComment().length() > 500) {
            throw new ReviewValidationException("Comment must be less than 500 characters");
        }
    }
}