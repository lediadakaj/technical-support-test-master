package com.orderdesk.dto;

import java.math.BigDecimal;

public record DashboardDto(
        long totalCustomers,
        long totalOrders,
        long pendingOrders,
        BigDecimal totalRevenue) {
}
