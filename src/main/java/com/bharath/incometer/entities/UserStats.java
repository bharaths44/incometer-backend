package com.bharath.incometer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_stats")
public class UserStats {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt;

    @Column(name = "total_transactions")
    private Long totalTransactions;

    @Column(name = "total_expenses")
    private Long totalExpenses;

    @Column(name = "total_income")
    private Long totalIncome;

    @Column(name = "total_expense_amount")
    private BigDecimal totalExpenseAmount;

    @Column(name = "total_income_amount")
    private BigDecimal totalIncomeAmount;

    @Column(name = "net_balance")
    private BigDecimal netBalance;

    @Column(name = "total_days_logged")
    private Long totalDaysLogged;

    @Column(name = "first_transaction_date")
    private LocalDateTime firstTransactionDate;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;
}
