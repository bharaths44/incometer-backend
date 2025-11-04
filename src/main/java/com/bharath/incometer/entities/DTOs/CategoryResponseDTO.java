package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.enums.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Category}
 */
public record CategoryResponseDTO(
	Long categoryId,
	java.util.UUID userId,
	String name,
	String icon,
	TransactionType type,
	LocalDateTime createdAt
) implements Serializable {
}