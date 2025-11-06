package com.bharath.incometer.entities;

import com.bharath.incometer.enums.BudgetFrequency;
import com.bharath.incometer.enums.BudgetType;
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
	@ToString.Exclude
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	@ToString.Exclude
	private Category category;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	// ✅ Start and end dates for the budget period
	@Column(name = "start_date")
	private LocalDate startDate;
	@Column(name = "end_date")
	private LocalDate endDate;

	// ✅ New fields
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetFrequency frequency;

	@Column(name = "is_active")
	private boolean active = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BudgetType type;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
