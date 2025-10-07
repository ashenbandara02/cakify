package com.cakify.enums;

public enum OrderStatus {
    PENDING("Order placed, waiting for confirmation"),
    CONFIRMED("Order confirmed by admin"),
    IN_PROGRESS("Order is being prepared"),
    READY("Order is ready for pickup/delivery"),
    DELIVERED("Order has been delivered"),
    CANCELLED("Order has been cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}