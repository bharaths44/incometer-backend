package com.bharath.incometer.utils;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class CategoryMatchingFuzzy {

	/**
	 * Find the closest matching category name using Levenshtein distance.
	 * Returns null if no match is found within threshold of 2.
	 */
	public String findClosestCategory(String categoryName, List<String> existingCategories) {
		String categoryNameLower = categoryName.toLowerCase();
		return existingCategories.stream()
								 .min(Comparator.comparingInt(c -> levenshteinDistance(c.toLowerCase(),
																					   categoryNameLower)))
								 .filter(c -> levenshteinDistance(c.toLowerCase(), categoryNameLower) <= 2)
								 .orElse(null);
	}

	/**
	 * Calculate Levenshtein distance between two strings.
	 */
	public int levenshteinDistance(String a, String b) {
		int[][] dp = new int[a.length() + 1][b.length() + 1];
		for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
		for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
		for (int i = 1; i <= a.length(); i++) {
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
			}
		}
		return dp[a.length()][b.length()];
	}
}

