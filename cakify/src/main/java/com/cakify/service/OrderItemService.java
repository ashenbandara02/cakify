package com.cakify.service;

import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import com.cakify.exception.OrderNotFoundException;
import com.cakify.exception.OrderValidationException;
import com.cakify.repository.OrderItemRepository;
import com.cakify.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for OrderItem business logic
 * Handles order item operations and validation
 */
@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Add item to an existing order
     */
    @Transactional
    public OrderItem addItemToOrder(Long orderId, OrderItem orderItem) {
        // Validate order exists
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new OrderNotFoundException(orderId);
        }
        
        Order order = orderOpt.get();
        
        // Validate order item
        validateOrderItem(orderItem);
        
        // Calculate total price
        orderItem.calculateTotalPrice();
        
        // Set the order relationship
        orderItem.setOrder(order);
        
        // Save order item
        OrderItem savedItem = orderItemRepository.save(orderItem);
        
        // Update order total amount
        order.calculateTotalAmount();
        orderRepository.save(order);
        
        return savedItem;
    }

    /**
     * Get all items for a specific order
     */
    public List<OrderItem> getItemsByOrderId(Long orderId) {
        // Validate order exists
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    /**
     * Get order item by ID
     */
    public Optional<OrderItem> getOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId);
    }

    /**
     * Update order item
     */
    @Transactional
    public OrderItem updateOrderItem(Long orderItemId, OrderItem updatedItem) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (!itemOpt.isPresent()) {
            throw new OrderNotFoundException("OrderItem not found with ID: " + orderItemId);
        }
        
        OrderItem existingItem = itemOpt.get();
        
        // Validate updated item
        validateOrderItem(updatedItem);
        
        // Update fields
        existingItem.setProductId(updatedItem.getProductId());
        existingItem.setProductName(updatedItem.getProductName());
        existingItem.setProductDescription(updatedItem.getProductDescription());
        existingItem.setUnitPrice(updatedItem.getUnitPrice());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setSpecialInstructions(updatedItem.getSpecialInstructions());
        
        // Recalculate total price
        existingItem.calculateTotalPrice();
        
        OrderItem savedItem = orderItemRepository.save(existingItem);
        
        // Update order total amount
        Order order = existingItem.getOrder();
        order.calculateTotalAmount();
        orderRepository.save(order);
        
        return savedItem;
    }

    /**
     * Delete order item
     */
    @Transactional
    public void deleteOrderItem(Long orderItemId) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (!itemOpt.isPresent()) {
            throw new OrderNotFoundException("OrderItem not found with ID: " + orderItemId);
        }
        
        OrderItem item = itemOpt.get();
        Order order = item.getOrder();
        
        orderItemRepository.deleteById(orderItemId);
        
        // Update order total amount
        order.calculateTotalAmount();
        orderRepository.save(order);
    }

    /**
     * Get items by product ID
     */
    public List<OrderItem> getItemsByProductId(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    /**
     * Search items by product name
     */
    public List<OrderItem> searchItemsByProductName(String productName) {
        return orderItemRepository.findByProductNameContainingIgnoreCase(productName);
    }

    /**
     * Get total quantity sold for a product
     */
    public Integer getTotalQuantitySoldByProduct(Long productId) {
        Integer total = orderItemRepository.getTotalQuantitySoldByProduct(productId);
        return total != null ? total : 0;
    }

    /**
     * Validate order item data
     */
    private void validateOrderItem(OrderItem orderItem) {
        if (orderItem.getProductId() == null) {
            throw new OrderValidationException("productId", "Product ID is required");
        }
        
        if (orderItem.getProductName() == null || orderItem.getProductName().trim().isEmpty()) {
            throw new OrderValidationException("productName", "Product name is required");
        }
        
        if (orderItem.getUnitPrice() == null || orderItem.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderValidationException("unitPrice", "Unit price must be greater than zero");
        }
        
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new OrderValidationException("quantity", "Quantity must be greater than zero");
        }
    }
}