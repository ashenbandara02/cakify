package com.cakify.service;

import com.cakify.dto.InquiryCategoryRequest;
import com.cakify.dto.InquiryCategoryResponse;
import com.cakify.entity.InquiryCategory;
import com.cakify.exception.CategoryNotFoundException;
import com.cakify.exception.InquiryValidationException;
import com.cakify.repository.InquiryCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * InquiryCategoryService - Business logic for inquiry category management
 * 
 * OOP Principles:
 * - Encapsulation: Business logic encapsulated in service methods
 * - Abstraction: Hides repository complexity from controllers
 * 
 * Design Patterns:
 * - Singleton Pattern: Spring creates single instance (@Service)
 * - Bridge Pattern: Bridges controller and repository layers
 * - Factory Method: Uses CategoryResponse.fromEntity()
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCategoryService {
    
    private final InquiryCategoryRepository categoryRepository;
    
    /**
     * Create a new inquiry category
     * @param request category data
     * @return created category response
     */
    public InquiryCategoryResponse createCategory(InquiryCategoryRequest request) {
        // Validate unique name
        validateUniqueCategoryName(request.getName(), null);
        
        // Create entity
        InquiryCategory category = new InquiryCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setIsActive(request.getIsActive());
        
        // Save and return
        InquiryCategory saved = categoryRepository.save(category);
        return InquiryCategoryResponse.fromEntity(saved);
    }
    
    /**
     * Update existing category
     * @param id category ID
     * @param request updated data
     * @return updated category response
     */
    public InquiryCategoryResponse updateCategory(Long id, InquiryCategoryRequest request) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        // Validate unique name (exclude current category)
        validateUniqueCategoryName(request.getName(), id);
        
        // Update fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setIsActive(request.getIsActive());
        
        InquiryCategory updated = categoryRepository.save(category);
        return InquiryCategoryResponse.fromEntity(updated);
    }
    
    /**
     * Delete a category
     * Business rule: Cannot delete category with existing inquiries
     * @param id category ID
     */
    public void deleteCategory(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        // Business rule: Check if category has inquiries
        if (!category.canBeDeleted()) {
            throw new InquiryValidationException(
                "category", 
                "Cannot delete category with existing inquiries. Please reassign or delete inquiries first."
            );
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * Get category by ID
     * @param id category ID
     * @return category response
     */
    @Transactional(readOnly = true)
    public InquiryCategoryResponse getCategoryById(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return InquiryCategoryResponse.fromEntity(category);
    }
    
    /**
     * Get all categories
     * @return list of all categories
     */
    @Transactional(readOnly = true)
    public List<InquiryCategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(InquiryCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only active categories (for customer dropdown)
     * @return list of active categories
     */
    @Transactional(readOnly = true)
    public List<InquiryCategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(InquiryCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Toggle category active status
     * @param id category ID
     * @return updated category
     */
    public InquiryCategoryResponse toggleActive(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        category.toggleActive();
        InquiryCategory updated = categoryRepository.save(category);
        return InquiryCategoryResponse.fromEntity(updated);
    }
    
    /**
     * Activate a category
     * @param id category ID
     * @return updated category
     */
    public InquiryCategoryResponse activateCategory(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        category.activate();
        InquiryCategory updated = categoryRepository.save(category);
        return InquiryCategoryResponse.fromEntity(updated);
    }
    
    /**
     * Deactivate a category
     * @param id category ID
     * @return updated category
     */
    public InquiryCategoryResponse deactivateCategory(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        category.deactivate();
        InquiryCategory updated = categoryRepository.save(category);
        return InquiryCategoryResponse.fromEntity(updated);
    }
    
    /**
     * Get inquiry count for a category
     * @param id category ID
     * @return number of inquiries
     */
    @Transactional(readOnly = true)
    public long getInquiryCount(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        return categoryRepository.countInquiriesByCategory(id);
    }
    
    /**
     * Get categories ordered by display order
     * @return ordered list of categories
     */
    @Transactional(readOnly = true)
    public List<InquiryCategoryResponse> getCategoriesOrderedByDisplayOrder() {
        List<InquiryCategory> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        return categories.stream()
                .map(InquiryCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category with inquiry count
     * @param id category ID
     * @return category response with inquiry count
     */
    @Transactional(readOnly = true)
    public InquiryCategoryResponse getCategoryWithInquiryCount(Long id) {
        InquiryCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        InquiryCategoryResponse response = InquiryCategoryResponse.fromEntity(category);
        // Inquiry count is already included via getInquiryCount() method in entity
        return response;
    }
    
    // ============== Private Helper Methods (Encapsulation) ==============
    
    /**
     * Validate category name is unique
     * @param name category name to check
     * @param excludeId ID to exclude from check (for updates)
     */
    private void validateUniqueCategoryName(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new InquiryValidationException("name", "Category name is required");
        }
        
        categoryRepository.findByNameIgnoreCase(name.trim())
                .ifPresent(existing -> {
                    // If updating, exclude current category from uniqueness check
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        throw new InquiryValidationException(
                            "name", 
                            "Category name '" + name + "' already exists"
                        );
                    }
                });
    }
}
