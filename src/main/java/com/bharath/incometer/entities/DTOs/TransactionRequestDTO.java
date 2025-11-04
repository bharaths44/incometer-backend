package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link Transaction}
 */
public record TransactionRequestDTO(
	java.util.UUID userId,
	@NotNull(message = "Category ID cannot be null")
	Long categoryId,
	@NotNull(message = "Amount cannot be null")
	@Positive
	BigDecimal amount,
	String description,
	Long paymentMethodId,
	LocalDate transactionDate,
	@NotNull(message = "Transaction type cannot be null")
	TransactionType transactionType
) {
}
