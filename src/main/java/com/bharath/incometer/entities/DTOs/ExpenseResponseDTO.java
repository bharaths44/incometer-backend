package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.Expense;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link Expense}
 */
public record ExpenseResponseDTO(Long expenseId,
                                 Long userUserId,
                                 CategoryDto category,
                                 BigDecimal amount,
                                 String description,
                                 PaymentMethodDto paymentMethod,
                                 LocalDate expenseDate)
	implements Serializable {
	/**
	 * DTO for {@link Category}
	 */
	public record CategoryDto(Long categoryId,
	                          String name,
	                          String icon) implements Serializable {
	}

	/**
	 * DTO for PaymentMethod
	 */
	public record PaymentMethodDto(Long paymentMethodId,
	                               String name,
	                               String displayName,
	                               String type) implements Serializable {
	}
}