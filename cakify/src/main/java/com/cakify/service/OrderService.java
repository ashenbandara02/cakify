package com.cakify.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import com.cakify.entity.Product;
import com.cakify.enums.OrderStatus;
import com.cakify.repository.OrderRepository;
import com.cakify.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cakify.exception.OrderNotFoundException;
import com.cakify.exception.OrderValidationException;
import com.cakify.exception.InvalidOrderStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.cakify.exception.InvalidOrderException;

import java.time.Duration;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BillService billService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProductRepository productRepository;

    // Create new order
    @Transactional
public Order createOrder(Order order) {
    // VALIDATION 1: Calculate order total from OrderItem prices
    // If totalPrice is already set in items (e.g., size-specific pricing from frontend), it will be used
    BigDecimal calculatedTotal = calculateOrderTotal(order);
    
    // If frontend already provided a valid totalAmount, use it; otherwise use calculated
    if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
        order.setTotalAmount(calculatedTotal);
    }
    
    validateDeliveryDate(order.getDeliveryDate());
    validateOrder(order);
    order.setStatus(OrderStatus.PENDING);
    order.setOrderDate(LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);
    
    //  Generate bill automatically
    try {
        billService.generateBillForOrder(savedOrder);
    } catch (Exception e) {
        // Log error but don't fail order creation
        System.err.println("Failed to generate bill for order " + savedOrder.getOrderId() + ": " + e.getMessage());
    }

    // ⭐ Send order confirmation email
    try {
        emailService.sendOrderConfirmationEmail(savedOrder);
    } catch (Exception e) {
        System.err.println("Failed to send order confirmation email: " + e.getMessage());
    }

    return savedOrder;
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
/**
 * Update order status with business rule validation
 * Integrates cancellation policy validation (Validation 3)
 */
public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
    // Fetch the order
    Optional<Order> orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isPresent()) {
        Order order = orderOpt.get();
        OrderStatus oldStatus = order.getStatus();
        
        // VALIDATION 3: If trying to cancel, check cancellation policies
        if (newStatus == OrderStatus.CANCELLED) {
            validateCancellation(order);
        }
        
        // Validate the status transition is allowed
        validateStatusTransition(oldStatus, newStatus);
        
        // Update the status
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        // Send appropriate email notification
        try {
            if (newStatus == OrderStatus.CANCELLED) {
                emailService.sendOrderCancellationEmail(updatedOrder);
                System.out.println("Order #" + orderId + " cancelled successfully. " +
                                 "Cancellation email sent to " + order.getCustomerEmail());
            } else {
                emailService.sendOrderStatusUpdateEmail(updatedOrder, newStatus);
                System.out.println("Order #" + orderId + " status updated: " +
                                 oldStatus + " → " + newStatus);
            }
        } catch (Exception e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
            // Don't fail the status update if email fails
        }
        
        return updatedOrder;
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

  /**
 * Validates that delivery date is valid for order placement
 * Rules:
 * 1. Must be in the future
 * 2. Must allow at least 2 days preparation time
 */
private void validateDeliveryDate(LocalDateTime deliveryDate) {
    LocalDateTime now = LocalDateTime.now();
    
    // RULE 1: Cannot be in the past
    if (deliveryDate.isBefore(now)) {
        throw new InvalidOrderException(
            "Delivery date cannot be in the past. " +
            "Please select a future date."
        );
    }
    
    // RULE 2: Must allow preparation time (minimum 2 days)
    LocalDateTime minimumDeliveryDate = now.plusDays(2);
    if (deliveryDate.isBefore(minimumDeliveryDate)) {
        throw new InvalidOrderException(
            "Custom cakes require at least 2 days preparation time. " +
            "Earliest available delivery: " + 
            minimumDeliveryDate.toLocalDate() + " at " +
            minimumDeliveryDate.toLocalTime()
        );
    }
    
    // OPTIONAL RULE 3: Reasonable maximum (prevent far future dates)
    LocalDateTime maxDeliveryDate = now.plusMonths(3);
    if (deliveryDate.isAfter(maxDeliveryDate)) {
        throw new InvalidOrderException(
            "Delivery date cannot be more than 3 months in advance. " +
            "Please contact us for bulk/event orders."
        );
    }
 }

 /**
 * Validates if an order can be cancelled based on business rules
 * Rules:
 * 1. Cannot cancel if already delivered
 * 2. Cannot cancel if being prepared (IN_PROGRESS)
 * 3. Cannot cancel within 24 hours of delivery
 * 4. Cannot cancel old orders (fraud protection)
 */
private void validateCancellation(Order order) {
    OrderStatus currentStatus = order.getStatus();
    LocalDateTime now = LocalDateTime.now();
    
    // RULE 1: Cannot cancel if already delivered
    if (currentStatus == OrderStatus.DELIVERED) {
        throw new InvalidOrderException(
            "Cannot cancel order that has already been delivered. " +
            "Order #" + order.getOrderId() + " was delivered. " +
            "Please contact customer support for returns/refunds."
        );
    }
    
    // RULE 2: Cannot cancel if currently being prepared
    if (currentStatus == OrderStatus.IN_PROGRESS) {
        throw new InvalidOrderException(
            "Cannot cancel order that is currently being prepared. " +
            "Your cake is already being made! " +
            "Please contact us immediately at [phone number]."
        );
    }
    
    // RULE 3: Cannot cancel if READY for pickup/delivery
    if (currentStatus == OrderStatus.READY) {
        throw new InvalidOrderException(
            "Cannot cancel order that is ready for delivery. " +
            "Your order is already completed and awaiting delivery. " +
            "Please contact customer support."
        );
    }
    
    // RULE 4: Cannot cancel within 24 hours of delivery
    LocalDateTime deliveryDate = order.getDeliveryDate();
    LocalDateTime cancellationDeadline = deliveryDate.minusHours(24);
    
    if (now.isAfter(cancellationDeadline)) {
        long hoursUntilDelivery = java.time.Duration.between(now, deliveryDate).toHours();
        throw new InvalidOrderException(
            "Cannot cancel order within 24 hours of delivery. " +
            "Your delivery is scheduled in " + hoursUntilDelivery + " hours. " +
            "Cancellation deadline was: " + cancellationDeadline.toLocalDate() + 
            " at " + cancellationDeadline.toLocalTime()
        );
    }
    
    // RULE 5: Cannot cancel very old orders (prevents fraud)
    LocalDateTime orderDate = order.getOrderDate();
    LocalDateTime maxCancellationDate = orderDate.plusDays(7);
    
    if (now.isAfter(maxCancellationDate)) {
        throw new InvalidOrderException(
            "Cannot cancel order more than 7 days after placement. " +
            "Order was placed on: " + orderDate.toLocalDate() + ". " +
            "Please contact customer support for assistance."
        );
    }
    
    // If we reach here, cancellation is allowed
    // Log for audit trail
    System.out.println("Order #" + order.getOrderId() + 
                      " cancellation validated. Current status: " + currentStatus);
 }

 /**
 * Auto-calculate order total from OrderItem prices
 * - If totalPrice is already set (e.g., from frontend with size-specific pricing), use it
 * - Otherwise, fetch product prices from database and calculate
 * - Sets unitPrice on each OrderItem (for price history)
 * - Returns the calculated total amount
 */
private BigDecimal calculateOrderTotal(Order order) {
    BigDecimal total = BigDecimal.ZERO;
    
    // Process each order item
    for (OrderItem item : order.getOrderItems()) {
        // Validate product exists
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new OrderValidationException(
                "Product with ID " + item.getProductId() + " not found"));
        
        // Validate quantity is positive
        if (item.getQuantity() <= 0) {
            throw new OrderValidationException(
                "Quantity must be greater than 0 for product: " + product.getName());
        }
        
        // Get current product base price
        BigDecimal basePrice = product.getPrice();
        
        // Validate price is positive
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderValidationException(
                "Product " + product.getName() + " has invalid price");
        }
        
        // Check if totalPrice is already set (e.g., from frontend with size-specific pricing)
        BigDecimal itemTotal;
        if (item.getTotalPrice() != null && item.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
            // Use the totalPrice provided by frontend (includes size multiplier)
            itemTotal = item.getTotalPrice();
            // Set unitPrice as base price for reference
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(basePrice);
            }
        } else {
            // Calculate from base price × quantity (fallback)
            itemTotal = basePrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setUnitPrice(basePrice);
            item.setTotalPrice(itemTotal);
        }
        
        // Add to running total
        total = total.add(itemTotal);
    }
    
    return total;
 }
}