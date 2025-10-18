package com.cakify.controller;

import com.cakify.dto.ProductResponse;
import com.cakify.entity.Category;
import com.cakify.entity.Product;
import com.cakify.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ProductController {

    private final ProductService productService;

    // GET /api/products - Get all products
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // GET /api/products/available - Get available products (public)
    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        List<ProductResponse> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    // GET /api/products/featured - Get featured products
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        List<ProductResponse> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    // GET /api/products/{id} - Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<ProductResponse> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/products/category/{categoryId} - Get products by category ID
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // GET /api/products/search?q={searchTerm} - Search products
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam("q") String searchTerm) {
        List<ProductResponse> products = productService.searchProducts(searchTerm);
        return ResponseEntity.ok(products);
    }

    // POST /api/products - Create new product (Admin only)
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRequest request) {
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());

            // Set category
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);

            product.setSizeList(request.getSizes());
            product.setFeatured(request.getFeatured());
            product.setImageUrl(request.getImageUrl());

            ProductResponse createdProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // PUT /api/products/{id} - Update product (Admin only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody CreateProductRequest request) {
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());

            // Set category
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);

            product.setSizeList(request.getSizes());
            product.setFeatured(request.getFeatured());
            product.setImageUrl(request.getImageUrl());

            Optional<ProductResponse> updatedProduct = productService.updateProduct(id, product);
            return updatedProduct.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // DELETE /api/products/{id} - Delete product (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Inner class for request body
    public static class CreateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Long categoryId;
        private List<String> sizes;
        private Boolean featured = false;
        private String imageUrl;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public List<String> getSizes() { return sizes; }
        public void setSizes(List<String> sizes) { this.sizes = sizes; }

        public Boolean getFeatured() { return featured; }
        public void setFeatured(Boolean featured) { this.featured = featured; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}