package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.enums.TransactionType;

import java.io.Serializable;

/**
 * DTO for {@link Category}
 */
public record CategoryRequestDTO(Long userId,
                                 String name,
                                 String icon,
                                 TransactionType type) implements Serializable {
}