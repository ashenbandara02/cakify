package com.cakify.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationResponseDto {

    private Long id;
    private String cakeName;
    private LocalDateTime createdAt;
    private Integer quantity;
    private Long productId;
    private Long orderId;
    private String customerName;
    private String status;
}
