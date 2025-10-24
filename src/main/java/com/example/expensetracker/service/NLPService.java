package com.example.expensetracker.service;

import com.example.expensetracker.entities.DTOs.ExpenseRequestDTO;
import com.example.expensetracker.entities.PendingCategory;
import com.example.expensetracker.entities.TransactionType;
import com.example.expensetracker.entities.Users;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class NLPService {

	private final StanfordCoreNLP pipeline;
	private static final Logger logger = LoggerFactory.getLogger(NLPService.class);

	public NLPService() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		props.setProperty("ner.useSUTime", "true");
		this.pipeline = new StanfordCoreNLP(props);
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
		BigDecimal amount = null;
		String categoryName = "";
		String paymentMethod = type == TransactionType.EXPENSE ? "Cash" : "Bank Account";
		LocalDate date = LocalDate.now();

		String lowerBody = body.toLowerCase();

		Annotation document = new Annotation(body);
		pipeline.annotate(document);

		List<String> tokens = new ArrayList<>();
		int moneyIndex = -1;

		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				tokens.add(word);
				if ("MONEY".equals(ner) && amount == null) {
					try {
						String cleaned = word.replaceAll("[^0-9.]", "");
						if (!cleaned.isEmpty()) {
							amount = new BigDecimal(cleaned);
							moneyIndex = tokens.size() - 1;
						}
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}

		// Fallback regex if NLP fails
		if (amount == null) {
			String[] fallbackTokens = body.split("\\s+");
			for (String token : fallbackTokens) {
				try {
					amount = new BigDecimal(token.replaceAll("[^0-9.]", ""));
					break;
				} catch (NumberFormatException ignored) {
				}
			}
			if (amount != null) {
				String amtStr = amount.toPlainString();
				String[] fallbackTokens2 = body.split("\\s+");
				for (int i = 0; i < fallbackTokens2.length; i++) {
					if (fallbackTokens2[i].replaceAll("[^0-9.]", "").equals(amtStr)) {
						moneyIndex = i;
						break;
					}
				}
			}
		}

		// Category extraction
		Set<String> stopWords = new HashSet<>(Arrays.asList("using", "in", "yesterday", "today", "tomorrow"));
		StringBuilder categoryBuilder = new StringBuilder();
		for (int i = moneyIndex + 1; i < tokens.size(); i++) {
			String word = tokens.get(i).toLowerCase();
			if (stopWords.contains(word)) break;
			categoryBuilder.append(tokens.get(i)).append(" ");
		}
		categoryName = categoryBuilder.toString().trim();
		if (categoryName.isEmpty()) {
			categoryName = type == TransactionType.EXPENSE ? "Miscellaneous" : "Bank Account";
		}

		// For income, if no "in", assume first word is category, rest is payment method
		if (type == TransactionType.INCOME) {
			String[] parts = categoryName.split("\\s+", 2);
			if (parts.length >= 1) {
				categoryName = parts[0];
				if (parts.length > 1 && !lowerBody.contains(" in ")) {
					paymentMethod = parts[1].toUpperCase();
				}
			}
		}

		// For expense, if no "using", assume first word is category, rest is payment method
		if (type == TransactionType.EXPENSE) {
			String[] parts = categoryName.split("\\s+", 2);
			if (parts.length >= 1) {
				categoryName = parts[0];
				if (parts.length > 1 && !lowerBody.contains("using")) {
					paymentMethod = parts[1].toUpperCase();
				}
			}
		}

		// Payment method
		if (type == TransactionType.EXPENSE && lowerBody.contains("using")) {
			int idx = lowerBody.indexOf("using") + 5;
			String[] parts = body.substring(idx).trim().split("\\s+");
			if (parts.length > 0) paymentMethod = parts[0].toUpperCase();
		} else if (type == TransactionType.INCOME && lowerBody.contains(" in ")) {
			int idx = lowerBody.indexOf(" in ") + 4;
			String[] parts = body.substring(idx).trim().split("\\s+");
			if (parts.length > 0) paymentMethod = parts[0].toUpperCase();
		}

		// Date
		if (lowerBody.contains("yesterday")) date = LocalDate.now().minusDays(1);
		else if (lowerBody.contains("tomorrow")) date = LocalDate.now().plusDays(1);

		return new ExtractionResult(amount, categoryName, paymentMethod, date);
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