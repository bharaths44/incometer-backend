package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.enums.TransactionType;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Category}
 */
public record CategoryRequestDTO(UUID userId,
                                 String name,
                                 String icon,
                                 TransactionType type) implements Serializable {
}