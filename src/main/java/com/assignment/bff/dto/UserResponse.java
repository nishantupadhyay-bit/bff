package com.assignment.bff.dto;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
