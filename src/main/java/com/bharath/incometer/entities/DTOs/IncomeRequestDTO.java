package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Income;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link Income}
 */
public record IncomeRequestDTO(
	@NotNull(message = "User ID cannot be null")
	Long userId,
	String source,
	@NotNull(message = "Amount cannot be null")
	@Positive
	BigDecimal amount,
	LocalDate receivedDate
) implements Serializable {
}
