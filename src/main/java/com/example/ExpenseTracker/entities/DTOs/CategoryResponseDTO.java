package com.example.ExpenseTracker.entities.DTOs;

import com.example.ExpenseTracker.entities.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.example.ExpenseTracker.entities.Category}
 */
public record CategoryResponseDTO(
		Long categoryId,
		Long userId,
		String name,
		TransactionType type,
		LocalDateTime createdAt
) implements Serializable {
}