package com.bharath.incometer.service;

import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.entities.Users;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionMessageHandler {

	private final ExpenseService expenseService;
	private final CategoryService categoryService;
	private final NLPService nlpService;

	private final Map<String, PendingCategory> pendingCategoryMap = new HashMap<>();

	public TransactionMessageHandler(ExpenseService expenseService,
	                                 CategoryService categoryService,
	                                 NLPService nlpService) {
		this.expenseService = expenseService;
		this.categoryService = categoryService;
		this.nlpService = nlpService;
	}

	public String handleTransactionMessage(Users user, String body) {
		if (pendingCategoryMap.containsKey(user.getPhoneNumber())) {
			return nlpService.handlePendingCategory(user, body, pendingCategoryMap, expenseService, categoryService);
		}

		if (body.toLowerCase().startsWith("expense ") || body.toLowerCase().contains("spent")) {
			return nlpService.handleExpense(user, body, pendingCategoryMap, expenseService, categoryService);
		}

		if (body.toLowerCase().startsWith("income ") || body.toLowerCase().contains("received")) {
			return nlpService.handleIncome(user, body, pendingCategoryMap, expenseService, categoryService);
		}

		return """
			⚠️ Unknown command. Try:
			- Register <Name>\
			
			- Expense <Amount> <Category> <Payment Method> <Optional:Date>\
			
			- Income <Amount> <Source> <Payment Method> <Optional:Date>""";
	}
}
