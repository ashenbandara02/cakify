package com.cakify.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationRequestDto {

    @NotBlank(message = "Cake name cannot be blank")
    @Size(max = 255, message = "Cake name cannot exceed 255 characters")
    private String cakeName;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName;

    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;
}
