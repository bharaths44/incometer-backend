package com.bharath.incometer.entities.DTOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserStatsResponseDTO(
        UUID userId,
        String userName,
        String userEmail,
        LocalDateTime accountCreatedAt,
        Long totalTransactions,
        Long totalExpenses,
        Long totalIncome,
        BigDecimal totalExpenseAmount,
        BigDecimal totalIncomeAmount,
        LocalDateTime firstTransactionDate
) {
}
