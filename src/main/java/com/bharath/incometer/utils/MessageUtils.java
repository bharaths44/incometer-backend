package com.bharath.incometer.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class MessageUtils {
	private static final Locale IN_LOCALE = Locale.forLanguageTag("en-IN");
	private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(IN_LOCALE);

	// Helper to format currency safely
	public String formatCurrency(Object amountObj) {
		try {
			switch (amountObj) {
				case null -> {
					return CURRENCY_FMT.format(BigDecimal.ZERO);
				}
				case BigDecimal ignored -> {
					return CURRENCY_FMT.format(amountObj);
				}
				case Number number -> {
					return CURRENCY_FMT.format(number.doubleValue());
				}
				default -> {
				}
			}
			// Try parse from string
			BigDecimal bd = new BigDecimal(amountObj.toString());
			return CURRENCY_FMT.format(bd);
		} catch (Exception e) {
			// fallback to raw string
			assert amountObj != null;
			return amountObj.toString();
		}
	}

	// Helper to calculate net = income - expense (handles nulls)
	public BigDecimal calculateNet(Object incomeObj, Object expenseObj) {
		BigDecimal income = toBigDecimalSafe(incomeObj);
		BigDecimal expense = toBigDecimalSafe(expenseObj);
		return income.subtract(expense);
	}

	public BigDecimal toBigDecimalSafe(Object o) {
		try {
			return switch (o) {
				case null -> BigDecimal.ZERO;
				case BigDecimal bigDecimal -> bigDecimal;
				case Number number -> BigDecimal.valueOf(number.doubleValue());
				default -> new BigDecimal(o.toString());
			};
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}


}
