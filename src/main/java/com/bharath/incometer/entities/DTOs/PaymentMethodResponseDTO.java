package com.bharath.incometer.entities.DTOs;

import com.bharath.incometer.entities.PaymentMethod;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link PaymentMethod}
 */
public record PaymentMethodResponseDTO(
	Long paymentMethodId,
	String name,
	String displayName,
	String lastFourDigits,
	String issuerName,
	String type,
	String icon,
	LocalDateTime createdAt
) implements Serializable {
}
