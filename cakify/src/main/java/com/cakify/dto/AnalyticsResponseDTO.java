package com.cakify.dto;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsResponseDTO {
    private long totalUsers;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private List<TopProductDTO> topSellingProducts;
    private List<MonthlySaleDTO> monthlySales;

    public AnalyticsResponseDTO() {
    }

    public AnalyticsResponseDTO(long totalUsers, int totalOrders, BigDecimal totalRevenue,
            List<TopProductDTO> topSellingProducts, List<MonthlySaleDTO> monthlySales) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.topSellingProducts = topSellingProducts;
        this.monthlySales = monthlySales;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<TopProductDTO> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopProductDTO> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public List<MonthlySaleDTO> getMonthlySales() {
        return monthlySales;
    }

    public void setMonthlySales(List<MonthlySaleDTO> monthlySales) {
        this.monthlySales = monthlySales;
    }
}
