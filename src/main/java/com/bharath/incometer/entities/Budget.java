package com.bharath.incometer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
	@Column(name = "budget_id")
	private Long budgetId;


	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private Users user;


	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	@ToString.Exclude
	private Category category;

	@Column(name = "amount_limit", nullable = false, precision = 10, scale = 2)
	private BigDecimal amountLimit;

	@Column(name = "month_limit")
	private Integer monthLimit;

	@Column(name = "year_limit")
	private Integer yearLimit;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		Class<?> oEffectiveClass = o instanceof HibernateProxy
								   ? ((HibernateProxy) o).getHibernateLazyInitializer()
														 .getPersistentClass()
								   : o.getClass();
		Class<?> thisEffectiveClass = this instanceof HibernateProxy
									  ? ((HibernateProxy) this).getHibernateLazyInitializer()
															   .getPersistentClass()
									  : this.getClass();
		if (thisEffectiveClass != oEffectiveClass) return false;
		Budget budget = (Budget) o;
		return getBudgetId() != null && Objects.equals(getBudgetId(), budget.getBudgetId());
	}

	@Override
	public final int hashCode() {
		return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
																	   .getPersistentClass()
																	   .hashCode() : getClass().hashCode();
	}
}

