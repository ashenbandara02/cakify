package com.cakify.exception;

/**
 * CategoryNotFoundException - Thrown when inquiry category is not found
 *
 * OOP Principles:
 * - Inheritance: Extends InquiryException
 * - Encapsulation: Encapsulates category not found error
 *
 * Design Patterns:
 * - Strategy Pattern: Part of exception handling strategy
 */
public class CategoryNotFoundException extends InquiryException {

    private static final String ERROR_CODE = "CATEGORY_NOT_FOUND";

    public CategoryNotFoundException(Long id) {
        super("Inquiry category not found with id: " + id, ERROR_CODE);
    }

    public CategoryNotFoundException(String name) {
        super("Inquiry category not found with name: " + name, ERROR_CODE);
    }

    public CategoryNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}