package com.cakify.service;

import com.cakify.dto.ProductResponse;
import com.cakify.entity.AvailabilityStatus;
import com.cakify.entity.Product;
import com.cakify.repository.ProductRepository;
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

    // Get all products
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get product by ID
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity);
    }

    // Get available products only (for public)
    public List<ProductResponse> getAvailableProducts() {
        List<Product> products = productRepository.findAvailableProducts();
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get featured products
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedTrue();
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Get products by category
    public List<ProductResponse> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Create new product
    public ProductResponse createProduct(Product product) {
        // Validate required fields
        validateProduct(product);

        // Set default values
        if (product.getAvailability() == null) {
            product.setAvailability(AvailabilityStatus.IN_STOCK);
        }
        if (product.getFeatured() == null) {
            product.setFeatured(false);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    // Update existing product
    public Optional<ProductResponse> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // Update fields
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setCategory(updatedProduct.getCategory());
                    existingProduct.setSizes(updatedProduct.getSizes());
                    existingProduct.setAvailability(updatedProduct.getAvailability());
                    existingProduct.setFeatured(updatedProduct.getFeatured());
                    existingProduct.setStockQuantity(updatedProduct.getStockQuantity());

                    // Keep existing image if new one is not provided
                    if (updatedProduct.getImageUrl() != null) {
                        existingProduct.setImageUrl(updatedProduct.getImageUrl());
                    }

                    Product savedProduct = productRepository.save(existingProduct);
                    return ProductResponse.fromEntity(savedProduct);
                });
    }

    // Delete product
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Toggle availability
    public Optional<ProductResponse> toggleAvailability(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    AvailabilityStatus newStatus = product.getAvailability() == AvailabilityStatus.IN_STOCK
                            ? AvailabilityStatus.UNAVAILABLE
                            : AvailabilityStatus.IN_STOCK;
                    product.setAvailability(newStatus);
                    Product savedProduct = productRepository.save(product);
                    return ProductResponse.fromEntity(savedProduct);
                });
    }

    // Search products by name
    public List<ProductResponse> searchProducts(String searchTerm) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(searchTerm);
        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Validation helper
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Product price must be non-negative");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("Product name must be less than 100 characters");
        }
    }
}