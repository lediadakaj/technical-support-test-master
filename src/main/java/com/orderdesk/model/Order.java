package com.orderdesk.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", check = @CheckConstraint(
        name = "delivered_order_has_date",
        constraint = "status <> 'DELIVERED' OR delivered_on IS NOT NULL"))
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Integer discountPercent;

    private LocalDate placedOn;

    private LocalDate deliveredOn;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<OrderItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public LocalDate getPlacedOn() {
        return placedOn;
    }

    public LocalDate getDeliveredOn() {
        return deliveredOn;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
