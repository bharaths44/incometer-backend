package com.bharath.incometer.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only entity mapped to a database view that provides aggregated user statistics.
 * This view automatically updates as transactions are added/modified/deleted.
 */
@Entity
@Immutable
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_stats_view")
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

