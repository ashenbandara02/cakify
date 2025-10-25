package com.cakify.exception;

/**
 * Exception thrown when a requested category is not found in the database
 * HTTP Status: 404 NOT FOUND
 * Demonstrates specific exception handling for resource not found scenarios
 */
public class CategoryNotFoundException extends CategoryException {
    
    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with ID: " + categoryId, "CATEGORY_NOT_FOUND");
    }
    
    public CategoryNotFoundException(String message) {
        super(message, "CATEGORY_NOT_FOUND");
    }
}
