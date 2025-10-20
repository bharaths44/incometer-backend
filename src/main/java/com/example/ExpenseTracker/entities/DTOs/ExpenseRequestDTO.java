package com.example.ExpenseTracker.entities.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link com.example.ExpenseTracker.entities.Expense}
 */
public record ExpenseRequestDTO(
		@NotNull(message = "User ID cannot be null")
		Long userId,
		@NotNull(message = "Category ID cannot be null")
		Long categoryId,
		@NotNull(message = "Amount cannot be null")
		@Positive
		BigDecimal amount,
		String description,
		String paymentMethod,
		LocalDate expenseDate
) implements Serializable {
}