package com.bharath.incometer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetAnalytics {
	private String categoryName;
	private BigDecimal spent;
	private BigDecimal limit;
	private BigDecimal remaining;
	private double usagePercentage;
	private boolean exceeded;
}
