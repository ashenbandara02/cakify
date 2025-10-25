package com.cakify.controller;

import com.cakify.dto.OrderNotificationRequestDto;
import com.cakify.dto.OrderNotificationResponseDto;
import com.cakify.service.OrderNotificationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class OrderNotificationsController {

    private final OrderNotificationsService service;

    // Create notification when order is placed
    @PostMapping("/create")
    public OrderNotificationResponseDto createNotification(
            @RequestBody @Valid OrderNotificationRequestDto dto) {
        return service.createNotification(dto);
    }

    // Get all notifications (dashboard)
    @GetMapping("/all")
    public List<OrderNotificationResponseDto> getAllNotifications() {
        return service.getAllNotifications();
    }
}
