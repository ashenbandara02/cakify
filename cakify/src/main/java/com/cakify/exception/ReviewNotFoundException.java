package com.cakify.exception;

/**
 * Exception thrown when a requested review is not found in the database
 * HTTP Status: 404 NOT FOUND
 * Demonstrates specific exception handling for resource not found scenarios
 */
public class ReviewNotFoundException extends ReviewException {
    
    public ReviewNotFoundException(Long reviewId) {
        super("Review not found with ID: " + reviewId, "REVIEW_NOT_FOUND");
    }
    
    public ReviewNotFoundException(String message) {
        super(message, "REVIEW_NOT_FOUND");
    }
}
