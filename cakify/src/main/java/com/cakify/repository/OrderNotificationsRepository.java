package com.cakify.repository;

import com.cakify.entity.OrderNotifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderNotificationsRepository extends JpaRepository<OrderNotifications, Long> {
}
