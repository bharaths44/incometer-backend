package com.example.expensetracker.service;

import com.example.expensetracker.entities.Users;
import com.example.expensetracker.entities.DTOs.ExpenseRequestDTO;
import com.example.expensetracker.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class WhatsAppMessageProcessor {

	private final UsersRepository usersRepository;
	private final ExpenseService expenseService;
	private final CategoryService categoryService;

	public WhatsAppMessageProcessor(UsersRepository usersRepository,
									ExpenseService expenseService,
									CategoryService categoryService) {
		this.usersRepository = usersRepository;
		this.expenseService = expenseService;
		this.categoryService = categoryService;
	}

	public String processMessage(String from, String body) {
		Users user = usersRepository.findByPhoneNumber(from);

		if (user == null) {
			return "❌ You are not registered. Send 'Register <Your Name>' to register.";
		}

		body = body.trim();

		// Registration command
		if (body.toLowerCase().startsWith("register ")) {
			return registerUser(from, body.substring(9).trim());
		}

		// Expense entry: e.g., "Expense 200 Food"
		if (body.toLowerCase().startsWith("expense ")) {
			return handleExpense(user, body.substring(8).trim());
		}

		return "⚠️ Unknown command. Try:\n- Register <Name>\n- Expense <Amount> <Category>";
	}

	private String registerUser(String from, String name) {
		if (usersRepository.existsByPhoneNumber(from)) {
			return "You are already registered.";
		}
		Users user = new Users();
		user.setName(name);
		user.setPhoneNumber(from);
		usersRepository.save(user);
		return "✅ Registered successfully! Your user ID: " + user.getUserId();
	}

	private String handleExpense(Users user, String body) {
		String[] parts = body.split(" ", 2);
		if (parts.length < 2) {
			return "❌ Invalid format. Use: Expense <Amount> <Category>";
		}

		try {
			BigDecimal amount = new BigDecimal(parts[0]);
			String categoryName = parts[1];

			// Find category by name (add a method in CategoryService)
			Long categoryId = categoryService.getCategoryIdByName(categoryName, user.getUserId());
			if (categoryId == null) {
				return "❌ Category not found. Please choose an existing category.";
			}

			ExpenseRequestDTO dto = new ExpenseRequestDTO(
					user.getUserId(),
					categoryId,
					amount,
					"Added via WhatsApp",
					"Cash",
					LocalDate.now()
			);

			expenseService.createExpense(dto);
			return "✅ Recorded ₹" + amount + " for " + categoryName;
		} catch (NumberFormatException e) {
			return "❌ Invalid amount. Please enter a numeric value.";
		}
	}
}
