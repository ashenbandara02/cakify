package com.cakify.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Product, Category, and Review modules
 * Demonstrates centralized exception handling and professional error management
 * Provides consistent error responses across all Product CRUD operations
 * 
 * OOP Principles Applied:
 * - Inheritance: Exception hierarchy with base and derived classes
 * - Polymorphism: Base exception handlers catch derived exceptions
 * - Encapsulation: Error details encapsulated in exception classes
 * - Separation of Concerns: Module-specific exception handling
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductGlobalExceptionHandler {
    
    // ==================== PRODUCT EXCEPTIONS ====================
    
    /**
     * Handle ProductNotFoundException - HTTP 404
     * Thrown when a product with specified ID doesn't exist
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Product Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle ProductValidationException - HTTP 400
     * Thrown when product data validation fails (invalid price, missing fields, etc.)
     */
    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ErrorResponse> handleProductValidationException(
            ProductValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Product Validation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle ImageUploadException - HTTP 400
     * Thrown when image upload fails (invalid format, size exceeded, storage error)
     */
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponse> handleImageUploadException(
            ImageUploadException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Image Upload Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general ProductException - HTTP 500
     * Fallback handler for any product-related exception not caught by specific handlers
     */
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductException(
            ProductException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Product Operation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // ==================== CATEGORY EXCEPTIONS ====================
    
    /**
     * Handle CategoryNotFoundException - HTTP 404
     * Thrown when a category with specified ID doesn't exist
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(
            CategoryNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Category Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle CategoryInUseException - HTTP 409
     * Thrown when attempting to delete a category that has associated products
     * Demonstrates foreign key constraint handling
     */
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUseException(
            CategoryInUseException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Category In Use",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle DuplicateCategoryException - HTTP 409
     * Thrown when attempting to create a category with an existing name
     * Demonstrates unique constraint handling
     */
    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCategoryException(
            DuplicateCategoryException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Duplicate Category",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle general CategoryException - HTTP 500
     * Fallback handler for any category-related exception not caught by specific handlers
     */
    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<ErrorResponse> handleCategoryException(
            CategoryException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Category Operation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // ==================== REVIEW EXCEPTIONS ====================
    
    /**
     * Handle ReviewNotFoundException - HTTP 404
     * Thrown when a review with specified ID doesn't exist
     */
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFoundException(
            ReviewNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Review Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle UnverifiedBuyerException - HTTP 403
     * Thrown when user attempts to review without having purchased the product
     * Demonstrates business logic validation and authorization
     */
    @ExceptionHandler(UnverifiedBuyerException.class)
    public ResponseEntity<ErrorResponse> handleUnverifiedBuyerException(
            UnverifiedBuyerException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Unverified Buyer",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle DuplicateReviewException - HTTP 409
     * Thrown when user attempts to submit multiple reviews for same product
     * Demonstrates business rule enforcement: one review per customer per product
     */
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReviewException(
            DuplicateReviewException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Duplicate Review",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle ReviewValidationException - HTTP 400
     * Thrown when review data validation fails (invalid rating, empty comment, etc.)
     */
    @ExceptionHandler(ReviewValidationException.class)
    public ResponseEntity<ErrorResponse> handleReviewValidationException(
            ReviewValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Review Validation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general ReviewException - HTTP 500
     * Fallback handler for any review-related exception not caught by specific handlers
     */
    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ErrorResponse> handleReviewException(
            ReviewException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Review Operation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // ==================== VALIDATION EXCEPTIONS ====================
    
    /**
     * Handle MethodArgumentNotValidException - HTTP 400
     * Thrown when @Valid annotation validation fails on request DTOs
     * Demonstrates Spring Validation integration
     * Returns detailed field-level validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        StringBuilder message = new StringBuilder("Validation failed: ");
        errors.forEach((field, msg) -> 
            message.append(field).append(" - ").append(msg).append("; ")
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            message.toString(),
            request.getDescription(false).replace("uri=", ""),
            "VALIDATION_ERROR"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
