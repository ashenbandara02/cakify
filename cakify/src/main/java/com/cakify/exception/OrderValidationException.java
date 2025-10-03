package com.cakify.exception;

/**
 * Exception thrown when order validation fails
 * Used for input validation errors
 */
public class OrderValidationException extends OrderException {
    
    public OrderValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
    
    public OrderValidationException(String field, String reason) {
        super("Validation failed for field '" + field + "': " + reason, "VALIDATION_ERROR");
    }
}