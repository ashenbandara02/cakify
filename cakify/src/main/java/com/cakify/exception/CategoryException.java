package com.cakify.exception;

/**
 * Base exception class for all category-related exceptions
 * Demonstrates inheritance hierarchy and custom exception design
 * Used for Category CRUD operations error handling
 */
public class CategoryException extends RuntimeException {
    
    private String errorCode;
    
    public CategoryException(String message) {
        super(message);
    }
    
    public CategoryException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CategoryException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
