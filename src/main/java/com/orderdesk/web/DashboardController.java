package com.orderdesk.web;

import com.orderdesk.dto.DashboardDto;
import com.orderdesk.model.OrderStatus;
import com.orderdesk.repository.CustomerRepository;
import com.orderdesk.repository.OrderRepository;
import com.orderdesk.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public DashboardController(CustomerRepository customerRepository,
                               OrderRepository orderRepository,
                               OrderService orderService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public DashboardDto stats() {
        var orders = orderRepository.findAll();
        BigDecimal revenue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(orderService::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pending = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();
        return new DashboardDto(
                customerRepository.count(),
                orders.size(),
                pending,
                revenue);
    }
}
