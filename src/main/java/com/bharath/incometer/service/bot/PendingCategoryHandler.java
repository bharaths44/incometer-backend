package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.DTOs.TransactionResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.service.CategoryService;
import com.bharath.incometer.service.TransactionService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PendingCategoryHandler {

	private final CategoryService categoryService;
	private final TemplateFactory templateFactory;

	public PendingCategoryHandler(CategoryService categoryService, TemplateFactory templateFactory) {
		this.categoryService = categoryService;
		this.templateFactory = templateFactory;
	}

	public WhatsAppMessageRequest handlePendingCategory(Users user, String body,
	                                                    Map<String, PendingCategory> pendingMap,
	                                                    TransactionService transactionService) {

		PendingCategory pending = pendingMap.get(user.getPhoneNumber());
		String phoneNumber = user.getPhoneNumber();
		return switch (pending.mode) {
			case "suggestion" -> handleSuggestion(body, pending, transactionService);
			case "creation" -> handleCreation(body, pending, pendingMap, transactionService, phoneNumber);
			case "confirmation" -> handleConfirmation(body, pending, pendingMap, transactionService, phoneNumber);
			case "undo" -> handleUndo(body, pending, pendingMap, transactionService, phoneNumber);
			default -> new WhatsAppMessageRequest("❓ Unknown pending mode.");
		};
	}

	private WhatsAppMessageRequest handleSuggestion(String body, PendingCategory pending,
	                                                TransactionService transactionService) {
		if (body.equals("1")) {
			return saveTransaction(pending, transactionService, pending.suggestedCategory);
		} else if (body.equals("2")) {
			return saveTransaction(pending, transactionService, pending.originalCategoryName);
		} else {
			return new WhatsAppMessageRequest("Reply with 1 or 2.");
		}
	}

	private WhatsAppMessageRequest handleCreation(String body, PendingCategory pending,
	                                              Map<String, PendingCategory> pendingMap,
	                                              TransactionService transactionService, String phoneNumber) {
		if (body.equalsIgnoreCase("yes")) {
			Long categoryId = categoryService.createCategoryForUser(pending.suggestedCategory,
			                                                        pending.userId,
			                                                        pending.type);
			TransactionRequestDTO dto = createTransactionDTO(pending, categoryId);
			transactionService.createTransaction(dto);
			pendingMap.remove(phoneNumber);
			return new WhatsAppMessageRequest(
				"✅ Created new category and recorded ₹" + pending.amount + " for " + pending.suggestedCategory);
		} else if (body.equalsIgnoreCase("no")) {
			pendingMap.remove(phoneNumber);
			return new WhatsAppMessageRequest("❌ Transaction cancelled.");
		} else {
			return new WhatsAppMessageRequest("❓ Reply 'Yes' to create the category or 'No' to cancel.");
		}
	}

	private WhatsAppMessageRequest handleConfirmation(String body, PendingCategory pending,
	                                                  Map<String, PendingCategory> pendingMap,
	                                                  TransactionService transactionService, String phoneNumber) {
		if (body.equalsIgnoreCase("undo")) {
			if (pending.createdAt.plusMinutes(1).isAfter(LocalDateTime.now())) {
				pendingMap.remove(phoneNumber);
				return new WhatsAppMessageRequest("❌ Transaction cancelled.");
			} else {
				return new WhatsAppMessageRequest("Already recorded.");
			}
		} else if (body.toLowerCase().contains("confirm")) {
			TransactionResponseDTO response = transactionService.createTransaction(createTransactionDTO(pending,
			                                                                                            pending.categoryId));
			pending.transactionId = response.transactionId();
			pending.mode = "undo";
			pending.createdAt = LocalDateTime.now();
			// pendingMap.put(phoneNumber, pending); already in map
			return templateFactory.createUndoTemplate(pending);
		} else if (body.toLowerCase().contains("delete")) {
			pendingMap.remove(phoneNumber);
			return new WhatsAppMessageRequest("❌ Cancelled.");
		} else if (body.toLowerCase().contains("edit")) {
			return new WhatsAppMessageRequest("Edit not implemented. Reply 'confirm' or 'delete'.");
		} else {
			return new WhatsAppMessageRequest("Invalid response. Please use the buttons.");
		}
	}

	private WhatsAppMessageRequest handleUndo(String body, PendingCategory pending,
	                                          Map<String, PendingCategory> pendingMap,
	                                          TransactionService transactionService, String phoneNumber) {
		if (body.equalsIgnoreCase("undo")) {
			if (pending.createdAt.plusMinutes(1).isAfter(LocalDateTime.now())) {
				transactionService.deleteTransaction(pending.transactionId, pending.userId);
				pendingMap.remove(phoneNumber);
				return new WhatsAppMessageRequest("❌ Transaction undone.");
			} else {
				pendingMap.remove(phoneNumber);
				return new WhatsAppMessageRequest("Too late to undo.");
			}
		} else {
			return new WhatsAppMessageRequest("Reply 'undo' to undo the transaction.");
		}
	}

	private WhatsAppMessageRequest saveTransaction(PendingCategory pending, TransactionService transactionService,
	                                               String categoryName) {
		Long categoryId = categoryService.getCategoryIdByName(categoryName, pending.userId);
		if (categoryId == null) {
			categoryId = categoryService.createCategoryForUser(categoryName, pending.userId, pending.type);
		}
		TransactionResponseDTO response = transactionService.createTransaction(createTransactionDTO(pending,
		                                                                                            categoryId));
		pending.transactionId = response.transactionId();
		pending.mode = "undo";
		pending.createdAt = LocalDateTime.now();
		return templateFactory.createUndoTemplate(pending);
	}

	private TransactionRequestDTO createTransactionDTO(PendingCategory pending, Long categoryId) {
		return new TransactionRequestDTO(pending.userId,
		                                 categoryId,
		                                 pending.amount,
		                                 "Added via WhatsApp",
		                                 pending.paymentMethodId,
		                                 pending.transactionDate,
		                                 pending.type);
	}
}
