package com.assignment.bff.dto;
public record CreateOrderRequest(
        Long userId,
        Long productId,
        Integer quantity
) {
}
