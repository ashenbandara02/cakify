package com.cakify.exception;

/**
 * Exception thrown when invalid order status transitions are attempted
 * Used for business rule violations
 */
public class InvalidOrderStatusException extends OrderException {
    
    public InvalidOrderStatusException(String message) {
        super(message, "INVALID_STATUS_TRANSITION");
    }
    
    public InvalidOrderStatusException(String currentStatus, String newStatus) {
        super("Invalid status transition from " + currentStatus + " to " + newStatus, 
              "INVALID_STATUS_TRANSITION");
    }
}