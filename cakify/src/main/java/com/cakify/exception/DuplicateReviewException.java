package com.cakify.exception;

/**
 * Exception thrown when a user attempts to submit multiple reviews for the same product
 * HTTP Status: 409 CONFLICT
 * Demonstrates business rule enforcement: one review per customer per product
 */
public class DuplicateReviewException extends ReviewException {
    
    public DuplicateReviewException(String email, Long productId) {
        super("User with email '" + email + "' has already submitted a review for product ID " + 
              productId + ". Each customer can only review a product once.", 
              "DUPLICATE_REVIEW");
    }
    
    public DuplicateReviewException(String message) {
        super(message, "DUPLICATE_REVIEW");
    }
}
