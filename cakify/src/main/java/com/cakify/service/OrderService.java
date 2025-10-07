package com.cakify.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cakify.entity.Order;
import com.cakify.enums.OrderStatus;
import com.cakify.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cakify.exception.OrderNotFoundException;
import com.cakify.exception.OrderValidationException;
import com.cakify.exception.InvalidOrderStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Create new order
    public Order createOrder(Order order) {
        validateOrder(order);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    // Get orders by customer email
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

   // update OrderStatus 
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Optional<Order> orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isPresent()) {
        Order order = orderOpt.get();
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
    throw new OrderNotFoundException(orderId);
    }

    // Update entire order
    public Order updateOrder(Long orderId, Order updatedOrder) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Update fields
            order.setCustomerName(updatedOrder.getCustomerName());
            order.setCustomerEmail(updatedOrder.getCustomerEmail());
            order.setCustomerPhone(updatedOrder.getCustomerPhone());
            order.setDeliveryAddress(updatedOrder.getDeliveryAddress());
            order.setTotalAmount(updatedOrder.getTotalAmount());
            order.setQuantity(updatedOrder.getQuantity());
            order.setDeliveryDate(updatedOrder.getDeliveryDate());
            order.setSpecialNotes(updatedOrder.getSpecialNotes());
            
            return orderRepository.save(order);
        }
        throw new OrderNotFoundException(orderId);
    }

    // Delete order
    public void deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
        } else {
            throw new OrderNotFoundException(orderId);
        }
    }

    // Get recent orders (for dashboard)
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }

    // Search orders by customer name
    public List<Order> searchOrdersByCustomerName(String name) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(name);
    }

    // Get order count by status
    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }

    /**
    * Validate order data before saving
    * Demonstrates business rule validation
    */
    private void validateOrder(Order order) {
    if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
        throw new OrderValidationException("customerName", "Customer name is required");
    }
    
    if (order.getCustomerEmail() == null || order.getCustomerEmail().trim().isEmpty()) {
        throw new OrderValidationException("customerEmail", "Customer email is required");
    }
    
    if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new OrderValidationException("totalAmount", "Total amount must be greater than zero");
    }
    
    if (order.getQuantity() == null || order.getQuantity() <= 0) {
        throw new OrderValidationException("quantity", "Quantity must be greater than zero");
    }
    
    // Email format validation (basic)
    if (!order.getCustomerEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        throw new OrderValidationException("customerEmail", "Invalid email format");
    }
  }

  /**
  * Validate order status transitions
  * Demonstrates business workflow rules
  */
private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
    // Define valid transitions
    switch (currentStatus) {
        case PENDING:
            if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusException(currentStatus.toString(), newStatus.toString());
            }
            break;
        case CONFIRMED:
            if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusException(currentStatus.toString(), newStatus.toString());
            }
            break;
        case IN_PROGRESS:
            if (newStatus != OrderStatus.READY && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusException(currentStatus.toString(), newStatus.toString());
            }
            break;
        case READY:
            if (newStatus != OrderStatus.DELIVERED) {
                throw new InvalidOrderStatusException(currentStatus.toString(), newStatus.toString());
            }
            break;
        case DELIVERED:
        case CANCELLED:
            throw new InvalidOrderStatusException("Cannot change status from " + currentStatus + " to " + newStatus);
    }
  }

    /**
    * Get all orders with pagination
    */
public Page<Order> getAllOrdersPaginated(Pageable pageable) {
    return orderRepository.findAll(pageable);
 }

/**
 * Get orders by status with pagination
 */
public Page<Order> getOrdersByStatusPaginated(OrderStatus status, Pageable pageable) {
    return orderRepository.findByStatus(status, pageable);
 }

   /**
   * Search orders by customer name with pagination
   */
   public Page<Order> searchOrdersByCustomerNamePaginated(String name, Pageable pageable) {
    return orderRepository.findByCustomerNameContainingIgnoreCase(name, pageable);
  }
}