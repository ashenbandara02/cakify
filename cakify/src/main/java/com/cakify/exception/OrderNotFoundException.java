package com.cakify.exception;

/**
 * Exception thrown when an order is not found in the system
 * Used for HTTP 404 responses
 */
public class OrderNotFoundException extends OrderException {
    
    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId, "ORDER_NOT_FOUND");
    }
    
    public OrderNotFoundException(String message) {
        super(message, "ORDER_NOT_FOUND");
    }
}