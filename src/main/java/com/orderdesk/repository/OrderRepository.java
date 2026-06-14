package com.orderdesk.repository;

import com.orderdesk.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderNumberContainingIgnoreCaseOrderByOrderNumber(String orderNumber);

    List<Order> findByCustomerIdOrderByPlacedOnDesc(Long customerId);

    long countByCustomerId(Long customerId);
}
