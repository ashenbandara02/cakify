package com.cakify.controller;

import com.cakify.dto.InquiryCategoryRequest;
import com.cakify.dto.InquiryCategoryResponse;
import com.cakify.service.InquiryCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing inquiry categories
 * Demonstrates:
 * - Repository Pattern (via service layer)
 * - Factory Method Pattern (CategoryResponse.fromEntity)
 * - Singleton Pattern (Spring-managed bean)
 */
@RestController
@RequestMapping("/api/inquiry-categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class InquiryCategoryController {
    
    private final InquiryCategoryService categoryService;
    
    /**
     * Create new inquiry category
     * POST /api/inquiry-categories
     * 
     * @param request category details
     * @return created category
     */
    @PostMapping
    public ResponseEntity<InquiryCategoryResponse> createCategory(@Valid @RequestBody InquiryCategoryRequest request) {
        InquiryCategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all inquiry categories
     * GET /api/inquiry-categories
     * 
     * @return list of all categories
     */
    @GetMapping
    public ResponseEntity<List<InquiryCategoryResponse>> getAllCategories() {
        List<InquiryCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get active inquiry categories (for dropdown)
     * GET /api/inquiry-categories/active
     * 
     * @return list of active categories
     */
    @GetMapping("/active")
    public ResponseEntity<List<InquiryCategoryResponse>> getActiveCategories() {
        List<InquiryCategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get category by ID
     * GET /api/inquiry-categories/{id}
     * 
     * @param id category ID
     * @return category details
     */
    @GetMapping("/{id}")
    public ResponseEntity<InquiryCategoryResponse> getCategoryById(@PathVariable Long id) {
        InquiryCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Update existing category
     * PUT /api/inquiry-categories/{id}
     * 
     * @param id category ID
     * @param request updated category details
     * @return updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<InquiryCategoryResponse> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody InquiryCategoryRequest request) {
        InquiryCategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete category
     * DELETE /api/inquiry-categories/{id}
     * 
     * @param id category ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Toggle category active status
     * PATCH /api/inquiry-categories/{id}/toggle
     * 
     * @param id category ID
     * @return updated category
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<InquiryCategoryResponse> toggleCategoryStatus(@PathVariable Long id) {
        InquiryCategoryResponse updated = categoryService.toggleActive(id);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get categories ordered by display order
     * GET /api/inquiry-categories/ordered
     * 
     * @return ordered list of categories
     */
    @GetMapping("/ordered")
    public ResponseEntity<List<InquiryCategoryResponse>> getCategoriesOrdered() {
        List<InquiryCategoryResponse> categories = categoryService.getCategoriesOrderedByDisplayOrder();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get category with inquiry count
     * GET /api/inquiry-categories/{id}/stats
     * 
     * @param id category ID
     * @return category with statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<InquiryCategoryResponse> getCategoryWithStats(@PathVariable Long id) {
        InquiryCategoryResponse category = categoryService.getCategoryWithInquiryCount(id);
        return ResponseEntity.ok(category);
    }
}
