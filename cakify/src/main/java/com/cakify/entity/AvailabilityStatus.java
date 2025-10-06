package com.cakify.entity;

public enum AvailabilityStatus {
    IN_STOCK,
    UNAVAILABLE,
    OUT_OF_STOCK;

    // Helper method to convert to frontend boolean
    public boolean isAvailable() {
        return this == IN_STOCK;
    }

    // Helper method to create from frontend boolean
    public static AvailabilityStatus fromBoolean(boolean available) {
        return available ? IN_STOCK : UNAVAILABLE;
    }
}