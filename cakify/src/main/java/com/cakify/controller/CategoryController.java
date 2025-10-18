package com.cakify.controller;

import com.cakify.dto.CategoryResponse;
import com.cakify.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class CategoryController {

    private final CategoryService categoryService;

    // GET all categories
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // GET category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create new category
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CategoryResponse created = categoryService.createCategory(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT update category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            return categoryService.updateCategory(id, newName)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}