package com.bharath.incometer.entities;

import com.bharath.incometer.enums.BudgetFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "budgets")
public class Budget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long budgetId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(name = "amount_limit", nullable = false, precision = 10, scale = 2)
	private BigDecimal amountLimit;

	// ✅ Start and end dates for the budget period
	@Column(name = "start_date")
	private LocalDate startDate;
	@Column(name = "end_date")
	private LocalDate endDate;

	// ✅ New fields
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetFrequency frequency;  // e.g., ONE_TIME, WEEKLY, MONTHLY, YEARLY

	@Column(name = "is_active")
	private boolean active = true;  // For disabling recurring budgets

	@CreationTimestamp
	private LocalDateTime createdAt;
}


