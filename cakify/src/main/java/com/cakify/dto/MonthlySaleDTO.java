package com.cakify.dto;

import java.math.BigDecimal;

public class MonthlySaleDTO {
    private String month;
    private BigDecimal totalRevenue;

    public MonthlySaleDTO() {
    }

    public MonthlySaleDTO(String month, BigDecimal totalRevenue) {
        this.month = month;
        this.totalRevenue = totalRevenue;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
