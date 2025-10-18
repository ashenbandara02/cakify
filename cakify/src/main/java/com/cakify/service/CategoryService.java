package com.cakify.service;

import com.cakify.dto.CategoryResponse;
import com.cakify.entity.Category;
import com.cakify.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Get all categories
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get category by ID
    public Optional<CategoryResponse> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponse);
    }

    // Create new category
    public CategoryResponse createCategory(String name) {
        // Validate input
        validateCategoryName(name);

        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }

        // Create and save
        Category category = new Category();
        category.setName(name.trim());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    // Update category
    public Optional<CategoryResponse> updateCategory(Long id, String newName) {
        // Validate input
        validateCategoryName(newName);

        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    // Check if new name already exists (and it's not the same category)
                    if (!existingCategory.getName().equalsIgnoreCase(newName) &&
                            categoryRepository.existsByNameIgnoreCase(newName)) {
                        throw new IllegalArgumentException("Category name already in use: " + newName);
                    }

                    existingCategory.setName(newName.trim());
                    Category updatedCategory = categoryRepository.save(existingCategory);
                    return mapToResponse(updatedCategory);
                });
    }

    // Delete category
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
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