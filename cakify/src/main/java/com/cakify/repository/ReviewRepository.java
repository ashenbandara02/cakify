package com.cakify.repository;

import com.cakify.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a specific product
    List<Review> findByProductId(Long productId);

    // Find all reviews by a specific email
    List<Review> findByEmail(String email);

    // Find review by product ID and email (check if already reviewed)
    Optional<Review> findByProductIdAndEmail(Long productId, String email);

    // Count total reviews for a product
    long countByProductId(Long productId);

    // Calculate average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> getAverageRatingByProductId(@Param("productId") Long productId);

    // Delete all reviews for a product (used when product is deleted)
    void deleteByProductId(Long productId);
}