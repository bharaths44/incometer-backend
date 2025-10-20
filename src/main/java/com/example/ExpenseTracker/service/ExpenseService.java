package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entities.Category;
import com.example.ExpenseTracker.entities.DTOs.ExpenseRequestDTO;
import com.example.ExpenseTracker.entities.DTOs.ExpenseResponseDTO;
import com.example.ExpenseTracker.entities.Expense;
import com.example.ExpenseTracker.entities.Users;
import com.example.ExpenseTracker.repository.CategoryRepository;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final CategoryRepository categoryRepository;
	private final UsersRepository usersRepository;

	public ExpenseService(ExpenseRepository expenseRepository,
						  CategoryRepository categoryRepository,
						  UsersRepository usersRepository) {
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		this.usersRepository = usersRepository;
	}


	private ExpenseResponseDTO toDTO(Expense expense) {
		Long expenseId = expense.getExpenseId();
		Long userId = expense.getUser().getUserId();
		Long categoryId = expense.getCategory().getCategoryId();
		BigDecimal amount = expense.getAmount();
		String description = expense.getDescription();
		String paymentMethod = expense.getPaymentMethod();
		LocalDate expenseDate = expense.getExpenseDate();
		LocalDateTime createdAt = expense.getCreatedAt();

		return new ExpenseResponseDTO(
				expenseId,
				userId,
				categoryId,
				amount,
				description,
				paymentMethod,
				expenseDate,
				createdAt
		);
	}

	public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
		Users user = usersRepository.findById(dto.userId())
									.orElseThrow(() -> new RuntimeException("User not found"));
		Category category = categoryRepository.findById(dto.categoryId())
											  .orElseThrow(() -> new RuntimeException("Category not found"));

		Expense expense = new Expense();
		expense.setUser(user);
		expense.setCategory(category);
		expense.setAmount(dto.amount());
		expense.setDescription(dto.description());
		expense.setPaymentMethod(dto.paymentMethod());
		expense.setExpenseDate(dto.expenseDate());

		Expense saved = expenseRepository.save(expense);
		return toDTO(saved);
	}

	public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto) {
		Expense expense = expenseRepository.findById(id)
										   .orElseThrow(() -> new RuntimeException("Expense not found"));
		Users user = usersRepository.findById(dto.userId())
									.orElseThrow(() -> new RuntimeException("User not found"));
		Category category = categoryRepository.findById(dto.categoryId())
											  .orElseThrow(() -> new RuntimeException("Category not found"));
		expense.setAmount(dto.amount());
		expense.setDescription(dto.description());
		expense.setPaymentMethod(dto.paymentMethod());
		expense.setExpenseDate(dto.expenseDate());
		expense.setUser(user);
		expense.setCategory(category);
		Expense updated = expenseRepository.save(expense);
		return toDTO(updated);
	}

	public void deleteExpense(Long id) {
		Expense expense = expenseRepository.findById(id)
										   .orElseThrow(() -> new RuntimeException("Expense not found"));
		expenseRepository.delete(expense);
	}

	public List<ExpenseResponseDTO> getAllExpenses() {
		return expenseRepository.findAll()
								.stream()
								.map(this::toDTO)
								.collect(Collectors.toList());
	}

}
