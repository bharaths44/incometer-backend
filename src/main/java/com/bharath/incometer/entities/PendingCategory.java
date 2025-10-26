package com.bharath.incometer.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PendingCategory {
	public Long userId;
	public BigDecimal amount;
	public String suggestedCategory;
	public String paymentMethod;
	public LocalDate expenseDate;
	public TransactionType type;
}
