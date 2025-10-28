package com.bharath.incometer.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for creating or updating {@link com.bharath.incometer.entities.PaymentMethod}
 */
public record PaymentMethodRequestDTO(
	@NotBlank(message = "Name cannot be blank")
	String name,
	String displayName,
	String lastFourDigits,
	String issuerName,
	@NotNull(message = "Type cannot be null")
	String type,
	// PaymentType as string
	String icon
) implements Serializable {
}
