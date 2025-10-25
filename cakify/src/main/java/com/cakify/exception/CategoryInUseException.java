package com.cakify.exception;

/**
 * Exception thrown when attempting to delete a category that has associated products
 * HTTP Status: 409 CONFLICT
 * Demonstrates foreign key constraint violation handling
 */
public class CategoryInUseException extends CategoryException {
    
    public CategoryInUseException(String categoryName) {
        super("Cannot delete category '" + categoryName + "' because it has associated products. " +
              "Please remove all products from this category first.", 
              "CATEGORY_IN_USE");
    }
    
    public CategoryInUseException(Long categoryId, int productCount) {
        super("Cannot delete category with ID " + categoryId + " because it has " + 
              productCount + " associated product(s). Please remove products first.", 
              "CATEGORY_IN_USE");
    }
}
