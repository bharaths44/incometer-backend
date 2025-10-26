package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.ExpenseRequestDTO;
import com.bharath.incometer.entities.PendingCategory;
import com.bharath.incometer.entities.TransactionType;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.utils.CategoryMatchingFuzzy;
import com.bharath.incometer.utils.PaymentMethodFormatter;
import com.bharath.incometer.utils.TransactionExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NLPService {

	private static final Logger logger = LoggerFactory.getLogger(NLPService.class);
	private final GeminiExtractionService geminiExtractionService;
	private final CategoryMatchingFuzzy categoryMatchingFuzzy;
	private final PaymentMethodFormatter paymentMethodFormatter;

	public NLPService(GeminiExtractionService geminiExtractionService,
					  CategoryMatchingFuzzy categoryMatchingFuzzy,
					  PaymentMethodFormatter paymentMethodFormatter) {
		this.geminiExtractionService = geminiExtractionService;
		this.categoryMatchingFuzzy = categoryMatchingFuzzy;
		this.paymentMethodFormatter = paymentMethodFormatter;
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
														  paymentMethodFormatter.normalizeForDb(pending.paymentMethod),
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
														  paymentMethodFormatter.normalizeForDb(pending.paymentMethod),
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

		TransactionExtractionResult result = geminiExtractionService.extractTransaction(body, TransactionType.EXPENSE);

		if (result.amount() == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName(), user.getUserId());
		if (categoryId != null) {
			ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
														  categoryId,
														  result.amount(),
														  "Added via WhatsApp",
														  paymentMethodFormatter.normalizeForDb(result.paymentMethod()),
														  result.date());
			expenseService.createExpense(dto);
			return "✅ Recorded ₹" + result.amount() + " for " + result.categoryName() + " using " + paymentMethodFormatter.toTitleCase(
					result.paymentMethod()) + " on " + result.date();
		}

		// Fuzzy match
		List<String> existingCategories = categoryService.getAllCategoryNamesForUserByType(user.getUserId(),
																						   TransactionType.EXPENSE);
		String closest = categoryMatchingFuzzy.findClosestCategory(result.categoryName(), existingCategories);

		if (closest != null) {
			PendingCategory pending = new PendingCategory();
			pending.userId = user.getUserId();
			pending.amount = result.amount();
			pending.suggestedCategory = closest;
			pending.paymentMethod = paymentMethodFormatter.normalizeForDb(result.paymentMethod());
			pending.expenseDate = result.date();
			pending.type = TransactionType.EXPENSE;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		// Create new
		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName(),
																   user.getUserId(),
																   TransactionType.EXPENSE);
		ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
													  newCategoryId,
													  result.amount(),
													  "Added via WhatsApp",
													  paymentMethodFormatter.normalizeForDb(result.paymentMethod()),
													  result.date());
		expenseService.createExpense(dto);
		String message = "✅ Created new category and recorded ₹" + result.amount() + " for " + result.categoryName() + " using " + paymentMethodFormatter.toTitleCase(
				result.paymentMethod()) + " on " + result.date();
		logger.info("Returning message: {}", message);
		return message;
	}

	public String handleIncome(Users user,
							   String body,
							   Map<String, PendingCategory> pendingMap,
							   ExpenseService expenseService,
							   CategoryService categoryService) {

		TransactionExtractionResult result = geminiExtractionService.extractTransaction(body, TransactionType.INCOME);

		if (result.amount() == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName(), user.getUserId());
		if (categoryId != null) {
			ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
														  categoryId,
														  result.amount(),
														  "Added via WhatsApp",
														  paymentMethodFormatter.normalizeForDb(result.paymentMethod()),
														  result.date());
			expenseService.createExpense(dto);
			return "✅ Recorded ₹" + result.amount() + " income for " + result.categoryName() + " in " + paymentMethodFormatter.toTitleCase(
					result.paymentMethod()) + " on " + result.date();
		}

		List<String> existingCategories = categoryService.getAllCategoryNamesForUserByType(user.getUserId(),
																						   TransactionType.INCOME);
		String closest = categoryMatchingFuzzy.findClosestCategory(result.categoryName(), existingCategories);

		if (closest != null) {
			PendingCategory pending = new PendingCategory();
			pending.userId = user.getUserId();
			pending.amount = result.amount();
			pending.suggestedCategory = closest;
			pending.paymentMethod = paymentMethodFormatter.normalizeForDb(result.paymentMethod());
			pending.expenseDate = result.date();
			pending.type = TransactionType.INCOME;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName(),
																   user.getUserId(),
																   TransactionType.INCOME);
		ExpenseRequestDTO dto = new ExpenseRequestDTO(user.getUserId(),
													  newCategoryId,
													  result.amount(),
													  "Added via WhatsApp",
													  paymentMethodFormatter.normalizeForDb(result.paymentMethod()),
													  result.date());
		expenseService.createExpense(dto);
		return "✅ Created new category and recorded ₹" + result.amount() + " income for " + result.categoryName() + " in " + paymentMethodFormatter.toTitleCase(
				result.paymentMethod()) + " on " + result.date();
	}

}
