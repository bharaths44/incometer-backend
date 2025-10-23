package com.example.expensetracker.entities.DTOs;

import com.example.expensetracker.entities.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.example.expensetracker.entities.Category}
 */
public record CategoryResponseDTO(
		Long categoryId,
		Long userId,
		String name,
		TransactionType type,
		LocalDateTime createdAt
) implements Serializable {
}