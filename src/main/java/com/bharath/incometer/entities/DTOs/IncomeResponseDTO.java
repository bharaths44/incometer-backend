package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Income;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link Income}
 */
public record IncomeResponseDTO(
	Long incomeId,
	Long userId,
	String source,
	BigDecimal amount,
	LocalDate receivedDate,
	LocalDateTime createdAt
) implements Serializable {
}
