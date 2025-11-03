package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.enums.BudgetFrequency;
import com.bharath.incometer.enums.BudgetType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link Budget}
 */
public record BudgetResponseDTO(
	Long budgetId,
	Long userId,
	Long categoryId,
	String categoryName,
	BigDecimal amount,
	LocalDate startDate,
	LocalDate endDate,
	BudgetFrequency frequency,
	BudgetType type,
	boolean active,
	LocalDateTime createdAt,
	BigDecimal spent,
	BigDecimal remaining,
	double progress
) implements Serializable {
}
