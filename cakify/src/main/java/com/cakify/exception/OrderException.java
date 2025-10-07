package com.cakify.exception;

/**
 * Base exception class for all order-related exceptions
 * Demonstrates inheritance hierarchy and custom exception design
 */
public class OrderException extends RuntimeException {
    
    private String errorCode;
    
    public OrderException(String message) {
        super(message);
    }
    
    public OrderException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OrderException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}