package com.cakify.service;

import com.cakify.dto.MonthlyRevenueDTO;
import com.cakify.dto.PopularProductDTO;
import com.cakify.dto.SummaryDTO;
import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import com.cakify.enums.OrderStatus;
import com.cakify.repository.OrderRepository;
import com.cakify.repository.OrderItemRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Monthly revenue for the last 6 months
     */
    public List<MonthlyRevenueDTO> getMonthlyRevenue() {
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(5).withDayOfMonth(1);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .filter(o -> o.getOrderDate().isAfter(sixMonthsAgo.minusDays(1)))
                .collect(Collectors.toList());

        Map<Month, BigDecimal> revenueByMonth = new HashMap<>();

        for (Order order : orders) {
            Month month = order.getOrderDate().getMonth();
            revenueByMonth.merge(month, order.getTotalAmount(), BigDecimal::add);
        }

        List<MonthlyRevenueDTO> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            Month month = date.getMonth();
            String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal value = revenueByMonth.getOrDefault(month, BigDecimal.ZERO);
            result.add(new MonthlyRevenueDTO(monthName, value));
        }

        return result;
    }

    /**
     * Most popular cakes/products based on total orders
     */
    public List<PopularProductDTO> getPopularProducts() {
        List<OrderItem> items = orderItemRepository.findAll();

        Map<String, Integer> productCount = new HashMap<>();
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                productCount.merge(item.getProduct().getName(), item.getQuantity(), Integer::sum);
            }
        }

        return productCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(e -> new PopularProductDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Summary stats for dashboard top cards
     */
    public SummaryDTO getSummary() {
        List<MonthlyRevenueDTO> monthlyRevenue = getMonthlyRevenue();
        List<PopularProductDTO> popular = getPopularProducts();

        BigDecimal totalRevenue = monthlyRevenue.stream()
                .map(MonthlyRevenueDTO::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgMonthly = totalRevenue.divide(BigDecimal.valueOf(monthlyRevenue.size()), 2,
                BigDecimal.ROUND_HALF_UP);

        MonthlyRevenueDTO bestMonth = monthlyRevenue.stream()
                .max(Comparator.comparing(MonthlyRevenueDTO::getRevenue))
                .orElse(new MonthlyRevenueDTO("N/A", BigDecimal.ZERO));

        int totalOrders = popular.stream()
                .mapToInt(PopularProductDTO::getOrders)
                .sum();

        return new SummaryDTO(totalRevenue, avgMonthly, bestMonth.getMonth(), bestMonth.getRevenue(), totalOrders);
    }
}
