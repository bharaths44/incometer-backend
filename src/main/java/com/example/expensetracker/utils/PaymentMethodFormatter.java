package com.example.expensetracker.utils;

import org.springframework.stereotype.Component;

@Component
public class PaymentMethodFormatter {

	/**
	 * Normalize a payment method for storage in the DB (lowercase, trimmed).
	 */
	public String normalizeForDb(String method) {
		if (method == null) return null;
		return method.trim().toLowerCase();
	}

	/**
	 * Convert a payment method to Title Case for user-facing messages.
	 */
	public String toTitleCase(String method) {
		if (method == null) return null;
		String m = method.trim().toLowerCase();
		if (m.isEmpty()) return m;
		String[] parts = m.split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i];
			if (p.isEmpty()) continue;
			sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
			if (i < parts.length - 1) sb.append(" ");
		}
		return sb.toString();
	}
}

