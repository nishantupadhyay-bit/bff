package com.assignment.bff.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 50, message = "Full name must not exceed 50 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 50, message = "Email must not exceed 50 characters")
        String email
) {
}
