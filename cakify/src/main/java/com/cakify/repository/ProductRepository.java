package com.cakify.repository;

import com.cakify.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category ID (NEW - for updated service)
    List<Product> findByCategoryId(Long categoryId);

    // Find products by category name (case insensitive)
    List<Product> findByCategoryNameIgnoreCase(String categoryName);

    // Find products by availability status
    @Query("SELECT p FROM Product p WHERE p.availability = true")
    List<Product> findAvailableProducts();

    // Find featured products
    List<Product> findByFeaturedTrue();

    // Find products by name (case insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Custom query to find products by category and availability
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:featured IS NULL OR p.featured = :featured)")
    List<Product> findProductsByCategoryAndFeatured(
            @Param("categoryId") Long categoryId,
            @Param("featured") Boolean featured);
}