package com.orderdesk.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailDto(
        Long id,
        String orderNumber,
        Long customerId,
        String customerName,
        String status,
        String placedOn,
        String deliveredOn,
        Integer discountPercent,
        List<OrderItemDto> items,
        BigDecimal subtotal,
        BigDecimal total) {
}
