package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.TransactionType;

import java.io.Serializable;

/**
 * DTO for {@link Category}
 */
public record CategoryRequestDTO(Long userId, String name, TransactionType type) implements Serializable {
}