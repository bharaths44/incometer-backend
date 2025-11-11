package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NLPService {

	private final PendingCategoryHandler pendingCategoryHandler;
	private final TransactionHandler transactionHandler;

	public NLPService(PendingCategoryHandler pendingCategoryHandler, TransactionHandler transactionHandler) {
		this.pendingCategoryHandler = pendingCategoryHandler;
		this.transactionHandler = transactionHandler;
	}

	public WhatsAppMessageRequest handlePendingCategory(Users user, String body,
	                                                    Map<String, PendingCategory> pendingMap,
	                                                    TransactionService transactionService) {
		return pendingCategoryHandler.handlePendingCategory(user, body, pendingMap, transactionService);
	}

	public WhatsAppMessageRequest handleExpense(Users user, String body, Map<String, PendingCategory> pendingMap) {
		return transactionHandler.handleTransaction(user,
		                                            body,
		                                            pendingMap,
		                                            TransactionType.EXPENSE);
	}

	public WhatsAppMessageRequest handleIncome(Users user, String body, Map<String, PendingCategory> pendingMap) {
		return transactionHandler.handleTransaction(user, body, pendingMap,
		                                            TransactionType.INCOME);
	}
}
