package com.cakify.exception;

/**
 * Thrown when inquiry data fails validation
 * HTTP Status: 400 BAD REQUEST
 */
public class InquiryValidationException extends InquiryException {

    public InquiryValidationException(String message) {
        super(message, "INQUIRY_VALIDATION_ERROR");
    }

    public InquiryValidationException(String field, String issue) {
        super(String.format("Validation error in field '%s': %s", field, issue),
                "INQUIRY_VALIDATION_ERROR");
    }

    public InquiryValidationException(String message, Throwable cause) {
        super(message, "INQUIRY_VALIDATION_ERROR", cause);
    }
}
