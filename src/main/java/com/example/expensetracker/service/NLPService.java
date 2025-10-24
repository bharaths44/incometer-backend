package com.example.expensetracker.service;

import com.example.expensetracker.entities.DTOs.ExpenseRequestDTO;
import com.example.expensetracker.entities.PendingCategory;
import com.example.expensetracker.entities.TransactionType;
import com.example.expensetracker.entities.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.client.GenerativeModel;
import com.google.genai.client.java.GenerativeModelFutures;
import com.google.genai.client.java.Part;
import com.google.genai.client.java.Response;
import com.google.genai.client.java.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class NLPService {

	private static final Logger logger = LoggerFactory.getLogger(NLPService.class);
	private final GenerativeModel geminiModel;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NLPService(@Value("${gemini.api.key}") String apiKey) {
		this.geminiModel = new GenerativeModel("gemini-1.5-flash", apiKey);
	}

	public String handlePendingCategory(Users user,
										String body,
										Map<String, PendingCategory> pendingMap,
										ExpenseService expenseService,
										CategoryService categoryService) {

		PendingCategory pending = pendingMap.get(user.getPhoneNumber());
		if (body.equalsIgnoreCase("yes")) {
			ExpenseRequestDTO dto = new ExpenseRequestDTO(pending.userId,
														  categoryService.getCategoryIdByName(pending.suggestedCategory,
																							  pending.userId),
														  pending.amount,
														  "Added via WhatsApp",
														  pending.paymentMethod,
														  pending.expenseDate);
			expenseService.createExpense(dto);
			pendingMap.remove(user.getPhoneNumber());
			return "✅ Recorded ₹" + pending.amount + " for " + pending.suggestedCategory;
		} else if (body.equalsIgnoreCase("no")) {
			Long categoryId = categoryService.createCategoryForUser(pending.suggestedCategory,
																	pending.userId,
																	pending.type);
			ExpenseRequestDTO dto = new ExpenseRequestDTO(pending.userId,
														  categoryId,
														  pending.amount,
														  "Added via WhatsApp",
														  pending.paymentMethod,
														  pending.expenseDate);
			expenseService.createExpense(dto);
			pendingMap.remove(user.getPhoneNumber());
			return "✅ Created new category and recorded ₹" + pending.amount + " for " + pending.suggestedCategory;
		} else {
			return "❓ Reply 'Yes' to use suggested category or 'No' to create new category.";
		}
	}

	public String handleExpense(Users user,
								String body,
								Map<String, PendingCategory> pendingMap,
								ExpenseService expenseService,
								CategoryService categoryService) {

		ExtractionResult result = extractAmountCategoryPaymentDate(body, TransactionType.EXPENSE);

		if (result.amount == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName, user.getUserId());
		if (categoryId != null) {
			ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
														  categoryId,
														  result.amount,
														  "Added via WhatsApp",
														  result.paymentMethod,
														  result.date);
			expenseService.createExpense(dto);
			return "✅ Recorded ₹" + result.amount + " for " + result.categoryName + " using " + result.paymentMethod + " on " + result.date;
		}

		// Fuzzy match
		List<String> existingCategories = categoryService.getAllCategoryNamesForUserByType(user.getUserId(),
																						   TransactionType.EXPENSE);
		String closest = findClosestCategory(result.categoryName, existingCategories);

		if (closest != null) {
			PendingCategory pending = new PendingCategory();
			pending.userId = user.getUserId();
			pending.amount = result.amount;
			pending.suggestedCategory = closest;
			pending.paymentMethod = result.paymentMethod;
			pending.expenseDate = result.date;
			pending.type = TransactionType.EXPENSE;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		// Create new
		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName,
																   user.getUserId(),
																   TransactionType.EXPENSE);
		ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
													  newCategoryId,
													  result.amount,
													  "Added via WhatsApp",
													  result.paymentMethod,
													  result.date);
		expenseService.createExpense(dto);
		return "✅ Created new category and recorded ₹" + result.amount + " for " + result.categoryName + " using " + result.paymentMethod + " on " + result.date;
	}

	public String handleIncome(Users user,
							   String body,
							   Map<String, PendingCategory> pendingMap,
							   ExpenseService expenseService,
							   CategoryService categoryService) {

		ExtractionResult result = extractAmountCategoryPaymentDate(body, TransactionType.INCOME);

		if (result.amount == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName, user.getUserId());
		if (categoryId != null) {
			ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
														  categoryId,
														  result.amount,
														  "Added via WhatsApp",
														  result.paymentMethod,
														  result.date);
			expenseService.createExpense(dto);
			return "✅ Recorded ₹" + result.amount + " income for " + result.categoryName + " in " + result.paymentMethod + " on " + result.date;
		}

		List<String> existingCategories = categoryService.getAllCategoryNamesForUserByType(user.getUserId(),
																						   TransactionType.INCOME);
		String closest = findClosestCategory(result.categoryName, existingCategories);

		if (closest != null) {
			PendingCategory pending = new PendingCategory();
			pending.userId = user.getUserId();
			pending.amount = result.amount;
			pending.suggestedCategory = closest;
			pending.paymentMethod = result.paymentMethod;
			pending.expenseDate = result.date;
			pending.type = TransactionType.INCOME;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName,
																   user.getUserId(),
																   TransactionType.INCOME);
		ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
													  newCategoryId,
													  result.amount,
													  "Added via WhatsApp",
													  result.paymentMethod,
													  result.date);
		expenseService.createExpense(dto);
		return "✅ Created new category and recorded ₹" + result.amount + " income for " + result.categoryName + " in " + result.paymentMethod + " on " + result.date;
	}

	// ---------------- Utility Methods ----------------

	private ExtractionResult extractAmountCategoryPaymentDate(String body, TransactionType type) {
		try {
			String prompt = "Extract structured expense/income information from the following text:\n" + "Text: \"" + body + "\"\n" + "Return JSON with fields: amount (number), category (string), payment_method (string), date (YYYY-MM-DD). " + "If a field cannot be determined, leave it null. " + "Transaction type: " + type.name();

			GenerativeModelFutures modelFutures = GenerativeModelFutures.from(geminiModel);
			CompletableFuture<Response> responseFuture = modelFutures.generateContent(new StringPart(prompt));
			String json = responseFuture.get().getText();

			logger.info("Gemini response: {}", json);

			// Simple JSON parsing
			// Expecting: {"amount": 250.0, "category": "Food", "payment_method": "Cash", "date": "2025-10-24"}
			Map<String, Object> map = objectMapper.readValue(json, Map.class);

			BigDecimal amount = map.get("amount") != null ? new BigDecimal(map.get("amount").toString()) : null;
			String category = map.getOrDefault("category", "").toString();
			String paymentMethod = map.getOrDefault("payment_method",
													type == TransactionType.EXPENSE ? "Cash" : "Bank Account")
									  .toString();
			LocalDate date = map.get("date") != null ? LocalDate.parse(map.get("date").toString()) : LocalDate.now();

			return new ExtractionResult(amount, category, paymentMethod, date);

		} catch (Exception e) {
			logger.error("Error in Gemini extraction", e);
			return new ExtractionResult(null,
										type == TransactionType.EXPENSE ? "Miscellaneous" : "Bank Account",
										type == TransactionType.EXPENSE ? "Cash" : "Bank Account",
										LocalDate.now());
		}
	}

	private String findClosestCategory(String categoryName, List<String> existingCategories) {
		String categoryNameLower = categoryName.toLowerCase();
		return existingCategories.stream()
								 .min(Comparator.comparingInt(c -> levenshteinDistance(c.toLowerCase(),
																					   categoryNameLower)))
								 .filter(c -> levenshteinDistance(c.toLowerCase(), categoryNameLower) <= 2)
								 .orElse(null);
	}

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

	// ---------------- Inner Classes ----------------

	public static class ExtractionResult {
		public BigDecimal amount;
		public String categoryName;
		public String paymentMethod;
		public LocalDate date;

		public ExtractionResult(BigDecimal amount, String categoryName, String paymentMethod, LocalDate date) {
			this.amount = amount;
			this.categoryName = categoryName;
			this.paymentMethod = paymentMethod;
			this.date = date;
		}
	}
}
