package com.bharath.incometer.models;

import com.bharath.incometer.enums.BudgetType;
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
	private BigDecimal amount;
	private BudgetType type;
	private BigDecimal remaining;
	private double percentage;
	private boolean exceeded;
}
