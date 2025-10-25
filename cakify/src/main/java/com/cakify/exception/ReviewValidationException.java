package com.cakify.exception;

/**
 * Exception thrown when review validation fails
 * HTTP Status: 400 BAD REQUEST
 * Covers scenarios like invalid rating range, empty comment, etc.
 */
public class ReviewValidationException extends ReviewException {
    
    public ReviewValidationException(String message) {
        super(message, "REVIEW_VALIDATION_ERROR");
    }
    
    public ReviewValidationException(String field, String reason) {
        super("Validation failed for field '" + field + "': " + reason, 
              "REVIEW_VALIDATION_ERROR");
    }
}
