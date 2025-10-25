package com.cakify.service;

import com.cakify.dto.OrderNotificationRequestDto;
import com.cakify.dto.OrderNotificationResponseDto;
import com.cakify.entity.OrderNotifications;
import com.cakify.repository.OrderNotificationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderNotificationsService {

    private final OrderNotificationsRepository repository;

    // Save notification when a new order is placed
    public OrderNotificationResponseDto createNotification(OrderNotificationRequestDto dto) {
        OrderNotifications notification = OrderNotifications.builder()
                .cakeName(dto.getCakeName())
                .quantity(dto.getQuantity())
                .productId(dto.getProductId())
                .orderId(dto.getOrderId())
                .customerName(dto.getCustomerName())
                .createdAt(LocalDateTime.now())
                .status(dto.getStatus() != null ? dto.getStatus() : "NEW")
                .build();

        OrderNotifications saved = repository.save(notification);

        return mapToResponse(saved);
    }

    // Get all notifications (for admin dashboard)
    public List<OrderNotificationResponseDto> getAllNotifications() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderNotificationResponseDto mapToResponse(OrderNotifications notification) {
        return OrderNotificationResponseDto.builder()
                .id(notification.getId())
                .cakeName(notification.getCakeName())
                .createdAt(notification.getCreatedAt())
                .quantity(notification.getQuantity())
                .productId(notification.getProductId())
                .orderId(notification.getOrderId())
                .customerName(notification.getCustomerName())
                .status(notification.getStatus())
                .build();
    }
}
