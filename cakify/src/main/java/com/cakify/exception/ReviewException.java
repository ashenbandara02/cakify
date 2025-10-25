package com.cakify.exception;

/**
 * Base exception class for all review-related exceptions
 * Demonstrates inheritance hierarchy and custom exception design
 * Used for Review CRUD operations and business logic validation
 */
public class ReviewException extends RuntimeException {
    
    private String errorCode;
    
    public ReviewException(String message) {
        super(message);
    }
    
    public ReviewException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ReviewException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ReviewException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
