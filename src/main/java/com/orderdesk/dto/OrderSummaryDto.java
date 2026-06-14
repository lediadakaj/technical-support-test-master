package com.orderdesk.dto;

import java.math.BigDecimal;

public record OrderSummaryDto(
        Long id,
        String orderNumber,
        String customerName,
        String status,
        String placedOn,
        BigDecimal total) {
}
