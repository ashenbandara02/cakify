package com.cakify.service;

import com.cakify.dto.CategoryResponse;
import com.cakify.entity.Category;
import com.cakify.exception.CategoryInUseException;
import com.cakify.exception.CategoryNotFoundException;
import com.cakify.exception.DuplicateCategoryException;
import com.cakify.repository.CategoryRepository;
import com.cakify.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // Get all categories
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get category by ID
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
    }

    // Create new category
    public CategoryResponse createCategory(String name) {
        // Validate input
        validateCategoryName(name);

        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryException("Category already exists: " + name);
        }

        // Create and save
        Category category = new Category();
        category.setName(name.trim());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    // Update category
    public CategoryResponse updateCategory(Long id, String newName) {
        // Validate input
        validateCategoryName(newName);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        // Check if new name already exists (and it's not the same category)
        if (!existingCategory.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new DuplicateCategoryException("Category name already in use: " + newName);
        }

        existingCategory.setName(newName.trim());
        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToResponse(updatedCategory);
    }

    // Delete category
    public void deleteCategory(Long id) {
        // Check if category exists
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with ID: " + id);
        }

        // Check if category is in use by any products
        if (!productRepository.findByCategoryId(id).isEmpty()) {
            throw new CategoryInUseException("Cannot delete category. It is currently in use by one or more products.");
        }

        categoryRepository.deleteById(id);
    }

    // Helper method to validate category name
    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Category name must be less than 50 characters");
        }
    }

    // Helper method to map entity to DTO
    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}