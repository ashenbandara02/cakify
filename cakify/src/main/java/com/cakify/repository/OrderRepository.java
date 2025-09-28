package com.cakify.repository;

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
}