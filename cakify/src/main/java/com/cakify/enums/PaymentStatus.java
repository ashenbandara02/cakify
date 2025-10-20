package com.cakify.enums;

public enum PaymentStatus {
    UNPAID("Bill generated, payment pending"),
    PAID_ON_DELIVERY("Cash collected at delivery"),
    CANCELLED("Order/Bill cancelled"),
    REFUNDED("Payment refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}