package com.bharath.incometer.entities;

import com.bharath.incometer.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_method_id")
	private Long paymentMethodId;

	@Column(nullable = false)
	private String name; // e.g. "Credit Card", "Cash", "UPI"

	@Column(name = "display_name")
	private String displayName;
	// e.g. "HDFC Credit Card", "Axis Bank Debit Card", "Paytm Wallet"
	// user-defined name for easier identification

	@Column(name = "last_four_digits", length = 4)
	private String lastFourDigits;
	// e.g. "1234" — never store full card numbers for security reasons!

	@Column(name = "issuer_name")
	private String issuerName;
	// e.g. "HDFC Bank", "SBI", "ICICI", etc.

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private PaymentType type;
	// e.g. "Credit Card", "Debit Card", "Wallet", "UPI", "Cash", "Bank Account"

	@Column(name = "icon")
	private String icon;
	// e.g. "credit-card", "wallet", "banknote"

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	@JsonIgnore
	private Users user;
	// null → system default; non-null → user-created

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		Class<?> oClass = o instanceof HibernateProxy
		                  ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
		                  : o.getClass();
		if (getClass() != oClass) return false;
		PaymentMethod that = (PaymentMethod) o;
		return Objects.equals(paymentMethodId, that.paymentMethodId);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(paymentMethodId);
	}
}
