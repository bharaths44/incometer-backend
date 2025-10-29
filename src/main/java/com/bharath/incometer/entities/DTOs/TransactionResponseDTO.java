package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
public record TransactionResponseDTO(Long transactionId,
                                     Long userUserId,
                                     CategoryDto category,
                                     BigDecimal amount,
                                     String description,
                                     PaymentMethodDto paymentMethod,
                                     LocalDate transactionDate,
                                     TransactionType transactionType,
                                     LocalDateTime createdAt) {
	/**
	 * DTO for {@link Category}
	 */
	public record CategoryDto(Long categoryId,
	                          String name,
	                          String icon) {
	}

	/**
	 * DTO for PaymentMethod
	 */
	public record PaymentMethodDto(Long paymentMethodId,
	                               String name,
	                               String displayName,
	                               String type) {
	}
}
