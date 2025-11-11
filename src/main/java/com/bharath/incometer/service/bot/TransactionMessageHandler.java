package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.service.TransactionService;
import com.bharath.incometer.service.TwilioService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionMessageHandler {

	private final TransactionService transactionService;
	private final NLPService nlpService;
	private final TwilioService twilioService;

	private final Map<String, PendingCategory> pendingCategoryMap = new HashMap<>();

	public TransactionMessageHandler(TransactionService transactionService,
	                                 NLPService nlpService,
	                                 TwilioService twilioService) {
		this.transactionService = transactionService;
		this.nlpService = nlpService;
		this.twilioService = twilioService;
	}

	public WhatsAppMessageRequest handleTransactionMessage(Users user, String body) {
		if (pendingCategoryMap.containsKey(user.getPhoneNumber())) {
			return nlpService.handlePendingCategory(user, body, pendingCategoryMap, transactionService);
		}

		if (body.toLowerCase().startsWith("expense ") || body.toLowerCase().contains("spent")) {
			return nlpService.handleExpense(user, body, pendingCategoryMap);
		}

		if (body.toLowerCase().startsWith("income ") || body.toLowerCase().contains("received")) {
			return nlpService.handleIncome(user, body, pendingCategoryMap);
		}

		return new WhatsAppMessageRequest(
			"Invalid format I couldn't parse that. Send transactions like: Expense 250 Coffee credit-card or Income " +
			"500 Salary bank Or reply Help for examples.");
	}

	@Scheduled(fixedRate = 60000)
	public void commitExpiredTransactions() {
		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, PendingCategory> entry : pendingCategoryMap.entrySet()) {
			PendingCategory pending = entry.getValue();
			if ("confirmation".equals(pending.mode) && pending.createdAt.plusMinutes(1).isBefore(LocalDateTime.now())) {
				TransactionRequestDTO dto = new TransactionRequestDTO(pending.userId,
				                                                      pending.categoryId,
				                                                      pending.amount,
				                                                      "Added via WhatsApp",
				                                                      pending.paymentMethodId,
				                                                      pending.transactionDate,
				                                                      pending.type);
				transactionService.createTransaction(dto);
				twilioService.sendWhatsAppMessage(entry.getKey(), "✅ Transaction recorded.");
				toRemove.add(entry.getKey());
			} else if ("undo".equals(pending.mode) && pending.createdAt.plusMinutes(1).isBefore(LocalDateTime.now())) {
				toRemove.add(entry.getKey());
			}
		}
		for (String key : toRemove) {
			pendingCategoryMap.remove(key);
		}
	}
}
