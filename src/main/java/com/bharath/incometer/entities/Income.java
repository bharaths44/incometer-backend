package com.bharath.incometer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "incomes")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Income {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "income_id")
	private Long incomeId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private Users user;

	private String source;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "received_date")
	private LocalDate receivedDate;

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
		Income income = (Income) o;
		return getIncomeId() != null && Objects.equals(getIncomeId(), income.getIncomeId());
	}

	@Override
	public final int hashCode() {
		return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
		                                                               .getPersistentClass()
		                                                               .hashCode() : getClass().hashCode();
	}
}

