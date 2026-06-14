package com.orderdesk.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
