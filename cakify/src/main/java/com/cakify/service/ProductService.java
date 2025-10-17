package com.cakify.service;

import com.cakify.dto.ProductResponse;
import com.cakify.entity.Category;
import com.cakify.entity.Product;
import com.cakify.repository.CategoryRepository;
import com.cakify.repository.ProductRepository;
import com.cakify.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    // Get all products with ratings
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Get product by ID with ratings
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToResponseWithRatings);
    }

    // Get available products only (for public)
    public List<ProductResponse> getAvailableProducts() {
        List<Product> products = productRepository.findAvailableProducts();
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Get featured products
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedTrue();
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Get products by category
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Create new product
    public ProductResponse createProduct(Product product) {
        // Validate required fields
        validateProduct(product);

        // Verify category exists
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        product.setCategory(category);

        // Set default values
        if (product.getFeatured() == null) {
            product.setFeatured(false);
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponseWithRatings(savedProduct);
    }

    // Update existing product
    public Optional<ProductResponse> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // Validate required fields
                    validateProduct(updatedProduct);

                    // Update category if provided
                    if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getId() != null) {
                        Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
                        existingProduct.setCategory(category);
                    }

                    // Update fields
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setSizes(updatedProduct.getSizes());
                    existingProduct.setFeatured(updatedProduct.getFeatured());

                    // Keep existing image if new one is not provided
                    if (updatedProduct.getImageUrl() != null) {
                        existingProduct.setImageUrl(updatedProduct.getImageUrl());
                    }

                    Product savedProduct = productRepository.save(existingProduct);
                    return mapToResponseWithRatings(savedProduct);
                });
    }

    // Delete product (and its reviews)
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            // Delete associated reviews
            reviewRepository.deleteByProductId(id);
            // Delete product
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search products by name
    public List<ProductResponse> searchProducts(String searchTerm) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm);
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Validation helper
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("Product name must be less than 100 characters");
        }
    }

    // Helper method to map with ratings
    private ProductResponse mapToResponseWithRatings(Product product) {
        Double averageRating = reviewRepository.getAverageRatingByProductId(product.getId())
                .orElse(0.0);
        Long reviewCount = reviewRepository.countByProductId(product.getId());
        return ProductResponse.fromEntity(product, averageRating, reviewCount);
    }
}