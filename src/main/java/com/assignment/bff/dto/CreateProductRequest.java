package com.assignment.bff.dto;
import java.math.BigDecimal;

public record CreateProductRequest(
        String name,
        String description,
        BigDecimal price,
        String currency,
        Integer stockQuantity
) {
}
