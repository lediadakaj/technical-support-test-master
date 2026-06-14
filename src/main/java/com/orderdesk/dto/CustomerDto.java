package com.orderdesk.dto;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String customerSince,
        long orderCount) {
}
