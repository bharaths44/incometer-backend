package com.bharath.incometer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAnalytics {
	private String categoryName;
	private BigDecimal totalSpent;
	private BigDecimal percentageOfTotal;
}
