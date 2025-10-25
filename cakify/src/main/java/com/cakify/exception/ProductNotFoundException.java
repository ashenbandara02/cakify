package com.cakify.exception;

/**
 * Exception thrown when a requested product is not found in the database
 * HTTP Status: 404 NOT FOUND
 * Demonstrates specific exception handling for resource not found scenarios
 */
public class ProductNotFoundException extends ProductException {
    
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId, "PRODUCT_NOT_FOUND");
    }
    
    public ProductNotFoundException(String message) {
        super(message, "PRODUCT_NOT_FOUND");
    }
}
