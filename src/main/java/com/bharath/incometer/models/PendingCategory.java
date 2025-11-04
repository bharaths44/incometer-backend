package com.bharath.incometer.models;

import com.bharath.incometer.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PendingCategory {
	public UUID userId;
	public BigDecimal amount;
	public String suggestedCategory;
	public Long paymentMethodId;
	public LocalDate transactionDate;
	public TransactionType type;
}
