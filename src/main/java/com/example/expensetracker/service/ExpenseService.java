package com.example.expensetracker.service;

import com.example.expensetracker.entities.Category;
import com.example.expensetracker.entities.DTOs.ExpenseRequestDTO;
import com.example.expensetracker.entities.DTOs.ExpenseResponseDTO;
import com.example.expensetracker.entities.Expense;
import com.example.expensetracker.entities.Users;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
		return new ExpenseResponseDTO(
				expense.getExpenseId(),
				expense.getUser().getUserId(),
				expense.getCategory().getCategoryId(),
				expense.getAmount(),
				expense.getDescription(),
				expense.getPaymentMethod(),
				expense.getExpenseDate(),
				expense.getCreatedAt()
		);
	}

	private Expense toEntity(ExpenseRequestDTO expenseRequestDTO) {
		Expense expense = new Expense();

		Users user = usersRepository.findById(expenseRequestDTO.userId())
									.orElseThrow(() -> new RuntimeException("User not found with id: " + expenseRequestDTO.userId()));

		Category category = categoryRepository.findById(expenseRequestDTO.categoryId())
											  .orElseThrow(() -> new RuntimeException("Category not found with id: " + expenseRequestDTO.categoryId()));

		// Validate that category belongs to the user
		if (!category.getUser().getUserId().equals(expenseRequestDTO.userId())) {
			throw new RuntimeException("Category does not belong to the specified user");
		}

		expense.setUser(user);
		expense.setCategory(category);
		expense.setAmount(expenseRequestDTO.amount());
		expense.setDescription(expenseRequestDTO.description());
		expense.setPaymentMethod(expenseRequestDTO.paymentMethod());
		expense.setExpenseDate(expenseRequestDTO.expenseDate());

		return expense;
	}

	private void validateExpenseRequest(ExpenseRequestDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("Expense request cannot be null");
		}

		if (dto.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (dto.categoryId() == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}

		if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}

		if (dto.expenseDate() == null) {
			throw new IllegalArgumentException("Expense date cannot be null");
		}

		if (dto.expenseDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Expense date cannot be in the future");
		}

		if (dto.paymentMethod() == null || dto.paymentMethod().trim().isEmpty()) {
			throw new IllegalArgumentException("Payment method cannot be null or empty");
		}
	}

	@Transactional
	public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
		validateExpenseRequest(dto);

		Expense expense = toEntity(dto);
		Expense saved = expenseRepository.save(expense);
		return toDTO(saved);
	}

	@Transactional
	public ExpenseResponseDTO updateExpense(Long expenseId, ExpenseRequestDTO dto) {
		if (expenseId == null) {
			throw new IllegalArgumentException("Expense ID cannot be null");
		}

		validateExpenseRequest(dto);

		Expense expense = expenseRepository.findById(expenseId)
										   .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));

		// Security check: ensure the expense belongs to the user making the request
		if (!expense.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("User does not have permission to update this expense");
		}

		// Validate and update category
		Category category = categoryRepository.findById(dto.categoryId())
											  .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.categoryId()));

		if (!category.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("Category does not belong to the specified user");
		}

		expense.setCategory(category);
		expense.setAmount(dto.amount());
		expense.setDescription(dto.description());
		expense.setPaymentMethod(dto.paymentMethod());
		expense.setExpenseDate(dto.expenseDate());

		Expense updated = expenseRepository.save(expense);
		return toDTO(updated);
	}

	@Transactional
	public void deleteExpense(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Expense ID cannot be null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Expense expense = expenseRepository.findById(id)
										   .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

		// Security check: ensure the expense belongs to the user
		if (!expense.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to delete this expense");
		}

		expenseRepository.delete(expense);
	}

	@Transactional(readOnly = true)
	public ExpenseResponseDTO getExpenseById(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Expense ID cannot be null");
		}

		Expense expense = expenseRepository.findById(id)
										   .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

		// Security check
		if (!expense.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to view this expense");
		}

		return toDTO(expense);
	}

	@Transactional(readOnly = true)
	public List<ExpenseResponseDTO> getAllExpenses() {
		return expenseRepository.findAll()
								.stream()
								.map(this::toDTO)
								.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ExpenseResponseDTO> getExpensesByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return expenseRepository.findByUserUserId(userId)
								.stream()
								.map(this::toDTO)
								.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ExpenseResponseDTO> getExpensesByUserIdAndDateRange(
			Long userId, LocalDate startDate, LocalDate endDate) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date cannot be null");
		}

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}

		return expenseRepository.findByUserUserIdAndExpenseDateBetween(userId, startDate, endDate)
								.stream()
								.map(this::toDTO)
								.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ExpenseResponseDTO> getExpensesByUserIdAndCategoryId(Long userId, Long categoryId) {
		if (userId == null || categoryId == null) {
			throw new IllegalArgumentException("User ID and Category ID cannot be null");
		}

		return expenseRepository.findByUserUserIdAndCategoryCategoryId(userId, categoryId)
								.stream()
								.map(this::toDTO)
								.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public BigDecimal getTotalExpensesByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return expenseRepository.sumAmountByUserId(userId);
	}

	@Transactional(readOnly = true)
	public BigDecimal getTotalExpensesByUserIdAndDateRange(
			Long userId, LocalDate startDate, LocalDate endDate) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date cannot be null");
		}

		return expenseRepository.sumAmountByUserIdAndDateRange(userId, startDate, endDate);
	}
}