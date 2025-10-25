package com.cakify.exception;

/**
 * Thrown when trying to reply to an already resolved inquiry
 * HTTP Status: 409 CONFLICT
 */
public class InquiryAlreadyResolvedException extends InquiryException {
    
    public InquiryAlreadyResolvedException(Long id) {
        super(String.format("Inquiry with id %d is already resolved. Use reopen endpoint first.", id), 
              "INQUIRY_ALREADY_RESOLVED");
    }
    
    public InquiryAlreadyResolvedException(String message) {
        super(message, "INQUIRY_ALREADY_RESOLVED");
    }
    
    public InquiryAlreadyResolvedException(String message, Throwable cause) {
        super(message, "INQUIRY_ALREADY_RESOLVED", cause);
    }
}
