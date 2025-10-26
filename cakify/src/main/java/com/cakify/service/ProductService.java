package com.cakify.service;

import com.cakify.dto.ProductResponse;
import com.cakify.entity.Category;
import com.cakify.entity.Product;
import com.cakify.exception.CategoryNotFoundException;
import com.cakify.exception.ImageUploadException;
import com.cakify.exception.ProductNotFoundException;
import com.cakify.exception.ProductValidationException;
import com.cakify.repository.CategoryRepository;
import com.cakify.repository.ProductRepository;
import com.cakify.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ReviewRepository reviewRepository,
                          ImageService imageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.imageService = imageService;
    }

    // Get all products with ratings
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponseWithRatings)
                .collect(Collectors.toList());
    }

    // Get product by ID with ratings
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToResponseWithRatings)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
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
            throw new ProductValidationException("Category is required");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new CategoryNotFoundException(product.getCategory().getId()));

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
                                .orElseThrow(() -> new CategoryNotFoundException(updatedProduct.getCategory().getId()));
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

    /**
     * Upload product image
     * @param productId - Product ID
     * @param file - Image file
     * @return Updated ProductResponse with new imageUrl
     */
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Delete old image if exists
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                imageService.deleteProductImage(product.getImageUrl());
                System.out.println("🗑️ Old image deleted for product: " + productId);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete old image: " + e.getMessage());
                // Continue anyway - old file might already be gone
            }
        }

        // Save new image
        try {
            String imageUrl = imageService.saveProductImage(file, productId);
            System.out.println("📸 New image saved for product " + productId + ": " + imageUrl);

            // Update product
            product.setImageUrl(imageUrl);
            Product savedProduct = productRepository.save(product);

            // Convert to response
            return mapToResponseWithRatings(savedProduct);
        } catch (Exception e) {
            throw new ImageUploadException("Failed to upload image for product " + productId, e);
        }
    }

    /**
     * Delete product image
     * @param productId - Product ID
     * @return Updated ProductResponse with imageUrl = null
     */
    public ProductResponse deleteProductImage(Long productId) {
        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Delete image file
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                imageService.deleteProductImage(product.getImageUrl());
                System.out.println("🗑️ Image deleted for product: " + productId);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete image: " + e.getMessage());
                // Continue anyway
            }
        }

        // Update product
        product.setImageUrl(null);
        Product savedProduct = productRepository.save(product);

        // Convert to response
        return mapToResponseWithRatings(savedProduct);
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
            throw new ProductValidationException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new ProductValidationException("Product price must be greater than 0");
        }
        if (product.getName().length() > 100) {
            throw new ProductValidationException("Product name must be less than 100 characters");
        }
    }

    /**
     * Update deleteProduct method to delete image before deleting product
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Delete image BEFORE deleting product
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                imageService.deleteProductImage(product.getImageUrl());
                System.out.println("🗑️ Image deleted with product: " + id);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete image during product deletion: " + e.getMessage());
                // Continue with product deletion anyway
            }
        }

        productRepository.delete(product);
        System.out.println("✅ Product deleted: " + id);
    }

    /**
     * Toggle product availability (true <-> false)
     * @param id - Product ID
     * @return Updated ProductResponse
     */
    public ProductResponse toggleAvailability(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        // Toggle availability
        product.setAvailability(!product.getAvailability());
        Product savedProduct = productRepository.save(product);
        
        System.out.println("✅ Product availability toggled: " + id + " -> " + savedProduct.getAvailability());
        return mapToResponseWithRatings(savedProduct);
    }


    // Helper method to map with ratings
    private ProductResponse mapToResponseWithRatings(Product product) {
        Double averageRating = reviewRepository.getAverageRatingByProductId(product.getId())
                .orElse(0.0);
        Long reviewCount = reviewRepository.countByProductId(product.getId());
        return ProductResponse.fromEntity(product, averageRating, reviewCount);
    }
}