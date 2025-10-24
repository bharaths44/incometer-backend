package com.example.expensetracker.utils;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionExtractionResult(
		BigDecimal amount, String categoryName, String paymentMethod, LocalDate date
) {

}

