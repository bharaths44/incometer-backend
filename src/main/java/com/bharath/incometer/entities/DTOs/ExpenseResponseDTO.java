package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Expense;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link Expense}
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