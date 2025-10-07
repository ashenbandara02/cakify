package com.cakify.repository;

import com.cakify.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 * Provides data access methods for order items
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items for a specific order
     */
    List<OrderItem> findByOrderOrderId(Long orderId);
    
    /**
     * Find all order items for a specific product
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * Find order items by product name (case insensitive)
     */
    List<OrderItem> findByProductNameContainingIgnoreCase(String productName);
    
    /**
     * Get total quantity sold for a specific product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Integer getTotalQuantitySoldByProduct(@Param("productId") Long productId);
    
    /**
     * Get order items with quantity greater than specified amount
     */
    List<OrderItem> findByQuantityGreaterThan(Integer quantity);
    
    /**
     * Delete all order items for a specific order
     */
    void deleteByOrderOrderId(Long orderId);
}