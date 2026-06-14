package com.orderdesk.service;

import com.orderdesk.dto.OrderDetailDto;
import com.orderdesk.dto.OrderItemDto;
import com.orderdesk.dto.OrderSummaryDto;
import com.orderdesk.model.Order;
import com.orderdesk.model.OrderItem;
import com.orderdesk.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public BigDecimal calculateSubtotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(Order order) {
        BigDecimal total = calculateSubtotal(order);
        if (order.getDiscountPercent() != null && order.getDiscountPercent() > 0) {
            total = total.multiply(BigDecimal.valueOf(100 - order.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return total;
    }

    public OrderSummaryDto toSummary(Order order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getStatus().name(),
                order.getPlacedOn().format(DATE_FORMAT),
                calculateTotal(order));
    }

    public OrderDetailDto toDetail(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()))
                                .setScale(2, RoundingMode.HALF_UP)))
                .toList();

        String deliveredOn = null;
        if (order.getStatus() == OrderStatus.DELIVERED && order.getDeliveredOn() != null) {
            deliveredOn = order.getDeliveredOn().format(DATE_FORMAT);
        }

        return new OrderDetailDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getStatus().name(),
                order.getPlacedOn().format(DATE_FORMAT),
                deliveredOn,
                order.getDiscountPercent(),
                items,
                calculateSubtotal(order),
                calculateTotal(order));
    }
}
