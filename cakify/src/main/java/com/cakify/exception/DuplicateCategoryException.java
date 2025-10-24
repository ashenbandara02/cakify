package com.cakify.exception;

/**
 * Exception thrown when attempting to create a category with a name that already exists
 * HTTP Status: 409 CONFLICT
 * Demonstrates unique constraint violation handling
 */
public class DuplicateCategoryException extends CategoryException {
    
    public DuplicateCategoryException(String categoryName) {
        super("Category with name '" + categoryName + "' already exists. " +
              "Please choose a different name.", 
              "DUPLICATE_CATEGORY");
    }
}
