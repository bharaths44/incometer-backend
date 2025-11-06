package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.enums.BudgetFrequency;
import com.bharath.incometer.enums.BudgetType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link Budget}
 */
public record BudgetRequestDTO(
	UUID userId,
	Long categoryId,
	BigDecimal amount,
	LocalDate startDate,
	LocalDate endDate,
	BudgetFrequency frequency,
	BudgetType type
) implements Serializable {
}

