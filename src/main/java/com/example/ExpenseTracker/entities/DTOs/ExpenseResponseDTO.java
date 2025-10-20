package com.example.ExpenseTracker.entities.DTOs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.example.ExpenseTracker.entities.Expense}
 */
public record ExpenseResponseDTO(
		Long expenseId,
		Long userId,
		Long categoryId,
		BigDecimal amount,
		String description,
		String paymentMethod,
		LocalDate expenseDate,
		LocalDateTime createdAt
) implements Serializable {
}