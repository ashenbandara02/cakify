package com.cakify.exception;

/**
 * Base exception class for all product-related exceptions
 * Demonstrates inheritance hierarchy and custom exception design
 * Used for Product CRUD operations error handling
 */
public class ProductException extends RuntimeException {
    
    private String errorCode;
    
    public ProductException(String message) {
        super(message);
    }
    
    public ProductException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProductException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
