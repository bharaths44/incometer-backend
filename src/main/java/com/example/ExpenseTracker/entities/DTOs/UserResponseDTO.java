package com.example.ExpenseTracker.entities.DTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UserResponseDTO(
		@NotNull Long userId,
		@NotNull String name,
		@NotNull String email,
		@NotNull String phoneNumber,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
