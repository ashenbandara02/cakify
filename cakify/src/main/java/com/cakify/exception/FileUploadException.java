package com.cakify.exception;

/**
 * FileUploadException - Thrown when file upload fails
 * 
 * OOP Principles:
 * - Inheritance: Extends InquiryException
 * - Encapsulation: Encapsulates file upload errors
 * 
 * Design Patterns:
 * - Strategy Pattern: Part of exception handling strategy
 */
public class FileUploadException extends InquiryException {
    
    private static final String ERROR_CODE = "FILE_UPLOAD_ERROR";
    
    public FileUploadException(String message) {
        super(message, ERROR_CODE);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileUploadException(String message, String errorCode) {
        super(message, errorCode);
    }
}
