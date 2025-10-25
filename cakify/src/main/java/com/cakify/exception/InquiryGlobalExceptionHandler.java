package com.cakify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for Inquiry module
 * Provides centralized exception handling with consistent error responses
 * Following the same pattern as Product, Category, and Review modules
 */
@ControllerAdvice
public class InquiryGlobalExceptionHandler {
    
    /**
     * Handle InquiryNotFoundException - HTTP 404
     * Thrown when an inquiry with the given ID doesn't exist
     */
    @ExceptionHandler(InquiryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInquiryNotFoundException(
            InquiryNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle InquiryValidationException - HTTP 400
     * Thrown when inquiry data fails validation rules
     */
    @ExceptionHandler(InquiryValidationException.class)
    public ResponseEntity<ErrorResponse> handleInquiryValidationException(
            InquiryValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle InquiryAlreadyResolvedException - HTTP 409
     * Thrown when trying to reply to an already resolved inquiry
     */
    @ExceptionHandler(InquiryAlreadyResolvedException.class)
    public ResponseEntity<ErrorResponse> handleInquiryAlreadyResolvedException(
            InquiryAlreadyResolvedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle InquiryOperationException - HTTP 500
     * Thrown when an inquiry operation fails unexpectedly
     */
    @ExceptionHandler(InquiryOperationException.class)
    public ResponseEntity<ErrorResponse> handleInquiryOperationException(
            InquiryOperationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle general InquiryException - HTTP 500
     * Fallback handler for any inquiry-related exceptions
     */
    @ExceptionHandler(InquiryException.class)
    public ResponseEntity<ErrorResponse> handleGeneralInquiryException(
            InquiryException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
