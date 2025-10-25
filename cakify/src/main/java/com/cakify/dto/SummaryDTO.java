package com.cakify.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
    private BigDecimal totalRevenue;
    private BigDecimal avgMonthlyRevenue;
    private String bestMonth;
    private BigDecimal bestMonthRevenue;
    private int totalOrders;
}
