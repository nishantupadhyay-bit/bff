package com.assignment.bff.dto;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long userId,
        Long productId,
        Integer quantity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
