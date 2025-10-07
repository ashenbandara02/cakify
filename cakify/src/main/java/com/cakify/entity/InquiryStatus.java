package com.cakify.entity;

public enum InquiryStatus {
    NEW,
    RESOLVED;
    
    // Helper method for default status
    public static InquiryStatus getDefaultStatus() {
        return NEW;
    }
    
    // Check if status is resolved
    public boolean isResolved() {
        return this == RESOLVED;
    }
    
    // Get status from string (case insensitive)
    public static InquiryStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return NEW;
        }
        try {
            return InquiryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NEW;
        }
    }
}