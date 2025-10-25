package com.cakify.exception;

/**
 * Thrown when an inquiry with the given ID is not found
 * HTTP Status: 404 NOT FOUND
 */
public class InquiryNotFoundException extends InquiryException {
    
    public InquiryNotFoundException(Long id) {
        super("Inquiry not found with id: " + id, "INQUIRY_NOT_FOUND");
    }
    
    public InquiryNotFoundException(String message) {
        super(message, "INQUIRY_NOT_FOUND");
    }
    
    public InquiryNotFoundException(String message, Throwable cause) {
        super(message, "INQUIRY_NOT_FOUND", cause);
    }
}
