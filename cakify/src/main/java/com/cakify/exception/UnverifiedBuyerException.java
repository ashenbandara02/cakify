package com.cakify.exception;

/**
 * Exception thrown when a user attempts to review a product without being a verified buyer
 * HTTP Status: 403 FORBIDDEN
 * Demonstrates business logic validation and authorization
 * Only customers who have completed orders containing the product can leave reviews
 */
public class UnverifiedBuyerException extends ReviewException {
    
    public UnverifiedBuyerException(String email, Long productId) {
        super("User with email '" + email + "' has not purchased product ID " + productId + ". " +
              "Only verified buyers who have completed orders can submit reviews.", 
              "UNVERIFIED_BUYER");
    }
    
    public UnverifiedBuyerException(String message) {
        super(message, "UNVERIFIED_BUYER");
    }
}
