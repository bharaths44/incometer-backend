package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.service.CategoryService;
import com.bharath.incometer.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionMessageHandler {

	private final TransactionService transactionService;
	private final CategoryService categoryService;
	private final NLPService nlpService;

	private final Map<String, PendingCategory> pendingCategoryMap = new HashMap<>();

	public TransactionMessageHandler(TransactionService transactionService,
	                                 CategoryService categoryService,
	                                 NLPService nlpService) {
		this.transactionService = transactionService;
		this.categoryService = categoryService;
		this.nlpService = nlpService;
	}

	public String handleTransactionMessage(Users user, String body) {
		if (pendingCategoryMap.containsKey(user.getPhoneNumber())) {
			return nlpService.handlePendingCategory(user,
			                                        body,
			                                        pendingCategoryMap,
			                                        transactionService,
			                                        categoryService);
		}

		if (body.toLowerCase().startsWith("expense ") || body.toLowerCase().contains("spent")) {
			return nlpService.handleExpense(user, body, pendingCategoryMap, transactionService, categoryService);
		}

		if (body.toLowerCase().startsWith("income ") || body.toLowerCase().contains("received")) {
			return nlpService.handleIncome(user, body, pendingCategoryMap, transactionService, categoryService);
		}

		return """
			⚠️ Unknown command. Try:
			- Register <Name>\
			
			- Expense <Amount> <Category> <Payment Method> <Optional:Date>\
			
			- Income <Amount> <Source> <Payment Method> <Optional:Date>""";
	}
}
