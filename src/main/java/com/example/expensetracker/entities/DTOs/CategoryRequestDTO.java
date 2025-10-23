package com.example.expensetracker.entities.DTOs;

import com.example.expensetracker.entities.TransactionType;

import java.io.Serializable;

/**
 * DTO for {@link com.example.expensetracker.entities.Category}
 */
public record CategoryRequestDTO(Long userId, String name, TransactionType type) implements Serializable {
}