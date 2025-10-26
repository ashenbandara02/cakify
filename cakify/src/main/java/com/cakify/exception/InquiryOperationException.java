package com.cakify.exception;

/**
 * Thrown when an inquiry operation fails unexpectedly
 * HTTP Status: 500 INTERNAL SERVER ERROR
 */
public class InquiryOperationException extends InquiryException {
    
    public InquiryOperationException(String message) {
        super(message, "INQUIRY_OPERATION_ERROR");
    }
    
    public InquiryOperationException(String message, Throwable cause) {
        super(message, "INQUIRY_OPERATION_ERROR", cause);
    }
}
