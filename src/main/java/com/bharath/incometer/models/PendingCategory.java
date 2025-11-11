package com.bharath.incometer.models;

import com.bharath.incometer.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PendingCategory {
	public UUID userId;
	public BigDecimal amount;
	public String suggestedCategory;
	public String originalCategoryName;
	public Long paymentMethodId;
	public LocalDate transactionDate;
	public TransactionType type;
	public String mode; // "suggestion" or "creation" or "confirmation" or "undo"
	public Long categoryId;
	public String paymentMethodName;
	public LocalDateTime createdAt;
	public Long transactionId;
}
