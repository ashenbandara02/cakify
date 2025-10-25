package com.cakify.entity;

import com.cakify.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;
    
    // Foreign key to Order (1:1 relationship)
    @Column(name = "order_id", unique = true, nullable = false)
    private Long orderId;
    
    // Bill identification
    @NotBlank(message = "Bill number is required")
    @Column(name = "bill_number", unique = true, nullable = false, length = 50)
    private String billNumber;
    
    // Customer Information
    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;
    
    @Column(name = "customer_phone", length = 15)
    private String customerPhone;
    
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;
    
    // Bill Amounts
    @NotNull(message = "Billing date is required")
    @Column(name = "billing_date", nullable = false)
    private LocalDateTime billingDate;
    
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "delivery_charges", precision = 10, scale = 2)
    private BigDecimal deliveryCharges = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    // Payment Information (COD)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "CASH_ON_DELIVERY";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    @Column(name = "payment_collected_date")
    private LocalDateTime paymentCollectedDate;
    
    @Column(name = "collected_by", length = 100)
    private String collectedBy;
    
    // Additional Info
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Bill() {}
    
    public Bill(Long orderId, String billNumber, String customerName, String customerEmail,
                BigDecimal subtotal, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.billNumber = billNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.billingDate = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.UNPAID;
    }
    
    // Getters and Setters
    public Long getBillId() {
        return billId;
    }
    
    public void setBillId(Long billId) {
        this.billId = billId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getBillNumber() {
        return billNumber;
    }
    
    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public LocalDateTime getBillingDate() {
        return billingDate;
    }
    
    public void setBillingDate(LocalDateTime billingDate) {
        this.billingDate = billingDate;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getDeliveryCharges() {
        return deliveryCharges;
    }
    
    public void setDeliveryCharges(BigDecimal deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPaymentCollectedDate() {
        return paymentCollectedDate;
    }
    
    public void setPaymentCollectedDate(LocalDateTime paymentCollectedDate) {
        this.paymentCollectedDate = paymentCollectedDate;
    }
    
    public String getCollectedBy() {
        return collectedBy;
    }
    
    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business method to calculate total
    public void calculateTotal() {
        this.totalAmount = this.subtotal
                .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO)
                .add(this.deliveryCharges != null ? this.deliveryCharges : BigDecimal.ZERO)
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }
}