package com.bharath.incometer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseSummary {
	private BigDecimal totalIncome;
	private BigDecimal totalExpense;
	private BigDecimal netSavings;
	private BigDecimal currentMonthExpense;
	private BigDecimal currentMonthIncome;
	private BigDecimal incomePercentageChange;
	private BigDecimal expensePercentageChange;
	private BigDecimal savingsPercentageChange;
}
