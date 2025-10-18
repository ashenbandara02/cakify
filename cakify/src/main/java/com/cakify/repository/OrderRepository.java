package com.cakify.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cakify.entity.Order;
import com.cakify.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Find orders by customer email
    List<Order> findByCustomerEmail(String customerEmail);
    
    // Find orders by user ID
    List<Order> findByUserId(Long userId);
    
    // Find orders by product ID
    List<Order> findByProductId(Long productId);
    
    // Find orders created between dates
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by status and user ID
    List<Order> findByStatusAndUserId(OrderStatus status, Long userId);
    
    // Custom query to find pending orders older than specified hours
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderDate < :cutoffTime")
    List<Order> findPendingOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Custom query to get total orders count by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countOrdersByStatus(@Param("status") OrderStatus status);
    
    // Find latest orders (for dashboard)
    List<Order> findTop10ByOrderByOrderDateDesc();
    
    // Custom query to find orders by customer name (case insensitive)
    @Query("SELECT o FROM Order o WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Order> findByCustomerNameContainingIgnoreCase(@Param("name") String name);

    /**
    * Find all orders with pagination
    */
    Page<Order> findAll(Pageable pageable);

    /**
    * Find orders by status with pagination
    */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
    * Find orders by customer email with pagination
    */
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);

    /**
    * Find orders by customer name with pagination (case insensitive)
    */
    @Query("SELECT o FROM Order o WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :name, '%'))")
        Page<Order> findByCustomerNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);






    // ========== METHOD FOR PRODUCT REVIEW VERIFICATION (PRODUCT CRUD RELATED) ==========
    /**
     * Check if a customer has a completed order containing a specific product
     * Used by Review System to verify only actual buyers can leave reviews
     *
     * @param email Customer email address
     * @param productId Product ID to check
     * @return true if customer has completed order with this product, false otherwise
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
            "WHERE o.customerEmail = :email " +
            "AND o.status = 'COMPLETED' " +
            "AND EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND oi.productId = :productId)")
    boolean existsByEmailAndProductIdAndStatus(
            @Param("email") String email,
            @Param("productId") Long productId
    );
    //=====================================================================================
}