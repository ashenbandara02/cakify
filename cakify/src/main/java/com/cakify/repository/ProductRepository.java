package com.cakify.repository;

import com.cakify.entity.AvailabilityStatus;
import com.cakify.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category
    List<Product> findByCategoryIgnoreCase(String category);

    // Find products by availability status
    List<Product> findByAvailability(AvailabilityStatus availability);

    // Find featured products
    List<Product> findByFeaturedTrue();

    // Find available products (for public display)
    @Query("SELECT p FROM Product p WHERE p.availability = 'IN_STOCK'")
    List<Product> findAvailableProducts();

    // Find products by name (case insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Custom query to find products by category and availability
    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:availability IS NULL OR p.availability = :availability)")
    List<Product> findProductsByCategoryAndAvailability(
            @Param("category") String category,
            @Param("availability") AvailabilityStatus availability);
}