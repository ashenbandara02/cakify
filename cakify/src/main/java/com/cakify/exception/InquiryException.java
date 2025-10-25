package com.cakify.exception;

/**
 * Base exception class for all inquiry-related exceptions
 * Provides consistent error handling across the Inquiry module
 */
public class InquiryException extends RuntimeException {
    
    private String errorCode;
    
    public InquiryException(String message) {
        super(message);
    }
    
    public InquiryException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public InquiryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InquiryException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
