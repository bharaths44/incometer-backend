package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Category}
 */
public record CategoryResponseDTO(
	Long categoryId,
	Long userId,
	String name,
	String icon,
	TransactionType type,
	LocalDateTime createdAt
) implements Serializable {
}