package com.orderdesk.web;

import com.orderdesk.dto.CustomerDto;
import com.orderdesk.dto.OrderSummaryDto;
import com.orderdesk.model.Customer;
import com.orderdesk.repository.CustomerRepository;
import com.orderdesk.repository.OrderRepository;
import com.orderdesk.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public CustomerController(CustomerRepository customerRepository,
                              OrderRepository orderRepository,
                              OrderService orderService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<CustomerDto> list(@RequestParam(name = "name", required = false) String name) {
        var customers = (name == null || name.isBlank())
                ? customerRepository.findAll()
                : customerRepository.findByNameContainingIgnoreCaseOrderByName(name.trim());
        return customers.stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Long id) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toDto(customer);
    }

    @GetMapping("/{id}/orders")
    public List<OrderSummaryDto> orders(@PathVariable Long id) {
        return orderRepository.findByCustomerIdOrderByPlacedOnDesc(id).stream()
                .map(orderService::toSummary)
                .toList();
    }

    private CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCustomerSince().format(DATE_FORMAT),
                orderRepository.countByCustomerId(customer.getId()));
    }
}
