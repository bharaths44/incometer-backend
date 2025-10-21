package com.example.ExpenseTracker.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

	@Setter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Long categoryId;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private Users user;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;


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
		Category category = (Category) o;
		return getCategoryId() != null && Objects.equals(getCategoryId(), category.getCategoryId());
	}

	@Override
	public final int hashCode() {
		return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
																	   .getPersistentClass()
																	   .hashCode() : getClass().hashCode();
	}
}

