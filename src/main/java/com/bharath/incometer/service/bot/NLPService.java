package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.service.CategoryService;
import com.bharath.incometer.service.PaymentMethodService;
import com.bharath.incometer.service.TransactionService;
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
	private final PaymentMethodService paymentMethodService;

	public NLPService(GeminiExtractionService geminiExtractionService,
	                  CategoryMatchingFuzzy categoryMatchingFuzzy,
	                  PaymentMethodFormatter paymentMethodFormatter,
	                  PaymentMethodService paymentMethodService) {
		this.geminiExtractionService = geminiExtractionService;
		this.categoryMatchingFuzzy = categoryMatchingFuzzy;
		this.paymentMethodFormatter = paymentMethodFormatter;
		this.paymentMethodService = paymentMethodService;
	}

	public String handlePendingCategory(Users user,
	                                    String body,
	                                    Map<String, PendingCategory> pendingMap,
	                                    TransactionService transactionService,
	                                    CategoryService categoryService) {

		PendingCategory pending = pendingMap.get(user.getPhoneNumber());
		if (body.equalsIgnoreCase("yes")) {
			TransactionRequestDTO dto = new TransactionRequestDTO(pending.userId,
			                                                      categoryService.getCategoryIdByName(pending.suggestedCategory,
			                                                                                          pending.userId),
			                                                      pending.amount,
			                                                      "Added via WhatsApp",
			                                                      pending.paymentMethodId,
			                                                      pending.transactionDate,
			                                                      pending.type);
			transactionService.createTransaction(dto);
			pendingMap.remove(user.getPhoneNumber());
			return "✅ Recorded ₹" + pending.amount + " for " + pending.suggestedCategory;
		} else if (body.equalsIgnoreCase("no")) {
			Long categoryId = categoryService.createCategoryForUser(pending.suggestedCategory,
			                                                        pending.userId,
			                                                        pending.type);
			TransactionRequestDTO dto = new TransactionRequestDTO(pending.userId,
			                                                      categoryId,
			                                                      pending.amount,
			                                                      "Added via WhatsApp",
			                                                      pending.paymentMethodId,
			                                                      pending.transactionDate,
			                                                      pending.type);
			transactionService.createTransaction(dto);
			pendingMap.remove(user.getPhoneNumber());
			return "✅ Created new category and recorded ₹" + pending.amount + " for " + pending.suggestedCategory;
		} else {
			return "❓ Reply 'Yes' to use suggested category or 'No' to create new category.";
		}
	}

	public String handleExpense(Users user,
	                            String body,
	                            Map<String, PendingCategory> pendingMap,
	                            TransactionService transactionService,
	                            CategoryService categoryService) {

		TransactionExtractionResult result = geminiExtractionService.extractTransaction(body, TransactionType.EXPENSE);

		if (result.amount() == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName(), user.getUserId());
		if (categoryId != null) {
			Long paymentMethodId = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
			                                           .getPaymentMethodId();
			TransactionRequestDTO dto = new TransactionRequestDTO(user.getUserId(),
			                                                      categoryId,
			                                                      result.amount(),
			                                                      "Added via WhatsApp",
			                                                      paymentMethodId,
			                                                      result.date(),
			                                                      TransactionType.EXPENSE);
			transactionService.createTransaction(dto);
			return "✅ Recorded ₹" + result.amount() + " for " + result.categoryName() + " using " +
			       paymentMethodFormatter.toTitleCase(
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
			pending.paymentMethodId = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
			                                              .getPaymentMethodId();
			pending.transactionDate = result.date();
			pending.type = TransactionType.EXPENSE;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		// Create new
		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName(),
		                                                           user.getUserId(),
		                                                           TransactionType.EXPENSE);
		Long paymentMethodId2 = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
		                                            .getPaymentMethodId();
		TransactionRequestDTO dto = new TransactionRequestDTO(user.getUserId(),
		                                                      newCategoryId,
		                                                      result.amount(),
		                                                      "Added via WhatsApp",
		                                                      paymentMethodId2,
		                                                      result.date(),
		                                                      TransactionType.EXPENSE);
		transactionService.createTransaction(dto);
		String message =
			"✅ Created new category and recorded ₹" + result.amount() + " for " + result.categoryName() + " using " +
			paymentMethodFormatter.toTitleCase(
				result.paymentMethod()) + " on " + result.date();
		logger.info("Returning message: {}", message);
		return message;
	}

	public String handleIncome(Users user,
	                           String body,
	                           Map<String, PendingCategory> pendingMap,
	                           TransactionService transactionService,
	                           CategoryService categoryService) {

		TransactionExtractionResult result = geminiExtractionService.extractTransaction(body, TransactionType.INCOME);

		if (result.amount() == null) {
			return "❌ Could not detect amount. Include a numeric value.";
		}

		Long categoryId = categoryService.getCategoryIdByName(result.categoryName(), user.getUserId());
		if (categoryId != null) {
			Long paymentMethodId = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
			                                           .getPaymentMethodId();
			TransactionRequestDTO dto = new TransactionRequestDTO(user.getUserId(),
			                                                      categoryId,
			                                                      result.amount(),
			                                                      "Added via WhatsApp",
			                                                      paymentMethodId,
			                                                      result.date(),
			                                                      TransactionType.INCOME);
			transactionService.createTransaction(dto);
			return "✅ Recorded ₹" + result.amount() + " income for " + result.categoryName() + " in " +
			       paymentMethodFormatter.toTitleCase(
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
			pending.paymentMethodId = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
			                                              .getPaymentMethodId();
			pending.transactionDate = result.date();
			pending.type = TransactionType.INCOME;
			pendingMap.put(user.getPhoneNumber(), pending);
			return "❓ Did you mean '" + closest + "'? Reply 'Yes' to use or 'No' to create new category.";
		}

		Long newCategoryId = categoryService.createCategoryForUser(result.categoryName(),
		                                                           user.getUserId(),
		                                                           TransactionType.INCOME);
		Long paymentMethodId2 = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
		                                            .getPaymentMethodId();
		TransactionRequestDTO dto = new TransactionRequestDTO(user.getUserId(),
		                                                      newCategoryId,
		                                                      result.amount(),
		                                                      "Added via WhatsApp",
		                                                      paymentMethodId2,
		                                                      result.date(),
		                                                      TransactionType.INCOME);
		transactionService.createTransaction(dto);
		return "✅ Created new category and recorded ₹" + result.amount() + " income for " + result.categoryName() +
		       " in " + paymentMethodFormatter.toTitleCase(
			result.paymentMethod()) + " on " + result.date();
	}

}
