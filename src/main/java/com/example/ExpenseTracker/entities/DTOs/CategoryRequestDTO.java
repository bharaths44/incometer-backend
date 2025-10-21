package com.example.ExpenseTracker.entities.DTOs;

import com.example.ExpenseTracker.entities.TransactionType;

import java.io.Serializable;

/**
 * DTO for {@link com.example.ExpenseTracker.entities.Category}
 */
public record CategoryRequestDTO(Long userId, String name, TransactionType type) implements Serializable {
}