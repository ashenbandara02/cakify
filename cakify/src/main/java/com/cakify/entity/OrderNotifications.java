package com.cakify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the cake/product
    @Column(name = "cake_name", nullable = false)
    @NotBlank(message = "Cake name cannot be blank")
    @Size(max = 255, message = "Cake name cannot exceed 255 characters")
    private String cakeName;

    // Time when the order was created
    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Order creation time cannot be null")
    private LocalDateTime createdAt;

    // Quantity ordered
    @Column(nullable = false)
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    // Product ID (reference to the cake/product)
    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be a positive number")
    private Long productId;

    // Order ID (reference to the order)
    @Column(name = "order_id", nullable = false)
    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be a positive number")
    private Long orderId;

    // Customer name
    @Column(name = "customer_name", nullable = false)
    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName;

    // The notification was read (can be NEW or SEEN)
    @Column(name = "status")
    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;
}
