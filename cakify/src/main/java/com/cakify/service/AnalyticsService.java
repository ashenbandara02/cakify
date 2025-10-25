package com.cakify.service;

import com.cakify.dto.AnalyticsResponseDTO;
import com.cakify.dto.MonthlySaleDTO;
import com.cakify.dto.TopProductDTO;
import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import com.cakify.repository.OrderItemRepository;
import com.cakify.repository.OrderRepository;
import com.cakify.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public AnalyticsService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    public AnalyticsResponseDTO getFullAnalytics() {
        long totalUsers = userRepository.count();
        int totalOrders = (int) orderRepository.count();

        // total revenue across all orders (skip nulls)
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly sales - last 6 months (including current)
        List<MonthlySaleDTO> monthlySales = computeLast6MonthsRevenue();

        // Top selling products - aggregate by productId & productName from OrderItem
        List<TopProductDTO> topProducts = computeTopProducts();

        return new AnalyticsResponseDTO(totalUsers, totalOrders, totalRevenue, topProducts, monthlySales);
    }

    private List<MonthlySaleDTO> computeLast6MonthsRevenue() {
        LocalDateTime now = LocalDateTime.now();
        // create map of Year-Month -> BigDecimal
        Map<String, BigDecimal> revenueMap = new HashMap<>();

        // initialize last 6 months keys to zero (so missing months show 0)
        for (int i = 5; i >= 0; i--) {
            LocalDateTime dt = now.minusMonths(i);
            Month m = dt.getMonth();
            String key = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + dt.getYear();
            revenueMap.put(key, BigDecimal.ZERO);
        }

        // sum orders by month-year
        for (Order order : orderRepository.findAll()) {
            LocalDateTime od = order.getOrderDate();
            if (od == null)
                continue;
            LocalDateTime sixMonthsAgo = now.minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);
            if (od.isBefore(sixMonthsAgo))
                continue;

            String key = od.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + od.getYear();
            BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            revenueMap.merge(key, amount, BigDecimal::add);
        }

        // build list in chronological order
        List<MonthlySaleDTO> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime dt = now.minusMonths(i);
            String key = dt.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + dt.getYear();
            result.add(new MonthlySaleDTO(dt.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    revenueMap.getOrDefault(key, BigDecimal.ZERO)));
        }

        return result;
    }

    private List<TopProductDTO> computeTopProducts() {
        List<OrderItem> items = orderItemRepository.findAll();

        // Key by composite productId + name (use productId if present)
        Map<String, TopProductDTO> stats = new HashMap<>();

        for (OrderItem item : items) {
            Long pid = item.getProductId();
            String name = item.getProductName() != null ? item.getProductName() : "Unknown product";
            String key = (pid != null ? pid.toString() : "0") + "|" + name;

            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            BigDecimal revenue;
            if (item.getTotalPrice() != null) {
                revenue = item.getTotalPrice();
            } else if (item.getUnitPrice() != null) {
                revenue = item.getUnitPrice().multiply(BigDecimal.valueOf(qty));
            } else {
                revenue = BigDecimal.ZERO;
            }

            if (stats.containsKey(key)) {
                TopProductDTO existing = stats.get(key);
                existing.setTotalQuantity(existing.getTotalQuantity() + qty);
                existing.setTotalRevenue(existing.getTotalRevenue().add(revenue));
            } else {
                TopProductDTO dto = new TopProductDTO(pid, name, qty, revenue);
                stats.put(key, dto);
            }
        }

        // sort by totalQuantity or totalRevenue — here we sort by totalQuantity desc
        return stats.values().stream()
                .sorted(Comparator.comparing(TopProductDTO::getTotalQuantity).reversed()
                        .thenComparing((TopProductDTO t) -> t.getTotalRevenue(), Comparator.reverseOrder()))
                .limit(6)
                .collect(Collectors.toList());
    }
}
