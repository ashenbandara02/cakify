package com.cakify.exception;

/**
 * Exception thrown when product validation fails
 * HTTP Status: 400 BAD REQUEST
 * Covers scenarios like invalid price, missing required fields, etc.
 */
public class ProductValidationException extends ProductException {
    
    public ProductValidationException(String message) {
        super(message, "PRODUCT_VALIDATION_ERROR");
    }
    
    public ProductValidationException(String field, String reason) {
        super("Validation failed for field '" + field + "': " + reason, 
              "PRODUCT_VALIDATION_ERROR");
    }
}
