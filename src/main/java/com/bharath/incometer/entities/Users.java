package com.bharath.incometer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID userId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(unique = true)
	private String phoneNumber;

	@Column(name = "auth0_sub", unique = true)
	private String auth0Sub;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

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
		Users users = (Users) o;
		return getUserId() != null && Objects.equals(getUserId(), users.getUserId());
	}

	@Override
	public final int hashCode() {
		return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
		                                                               .getPersistentClass()
		                                                               .hashCode() : getClass().hashCode();
	}
}
