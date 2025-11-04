package com.bharath.incometer.service;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.BudgetRequestDTO;
import com.bharath.incometer.entities.DTOs.BudgetResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.BudgetRepository;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.TransactionRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetService {

	private final BudgetRepository budgetRepository;
	private final CategoryRepository categoryRepository;
	private final UsersRepository usersRepository;
	private final TransactionRepository transactionRepository;

	public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
	                     UsersRepository usersRepository, TransactionRepository transactionRepository) {
		this.budgetRepository = budgetRepository;
		this.categoryRepository = categoryRepository;
		this.usersRepository = usersRepository;
		this.transactionRepository = transactionRepository;
	}

	private BudgetResponseDTO toDTO(Budget budget) {
		BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryIdAndTypeAndDateRange(budget.getUser()
		                                                                                                 .getUserId(),
		                                                                                           budget.getCategory()
		                                                                                                 .getCategoryId(),
		                                                                                           TransactionType.EXPENSE,
		                                                                                           budget.getStartDate(),
		                                                                                           budget.getEndDate());
		if (spent == null) {
			spent = BigDecimal.ZERO;
		}
		BigDecimal remaining = budget.getAmount().subtract(spent);
		double progress = 0.0;
		if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
			progress = spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
		}
		return new BudgetResponseDTO(budget.getBudgetId(),
		                             budget.getUser().getUserId(),
		                             budget.getCategory().getCategoryId(),
		                             budget.getCategory().getName(),
		                             budget.getAmount(),
		                             budget.getStartDate(),
		                             budget.getEndDate(),
		                             budget.getFrequency(),
		                             budget.getType(),
		                             budget.isActive(),
		                             budget.getCreatedAt(),
		                             spent,
		                             remaining,
		                             progress);
	}

	private Budget toEntity(BudgetRequestDTO dto) {
		Budget budget = new Budget();

		Users user = usersRepository.findById(dto.userId())
		                            .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.userId()));

		Category category = categoryRepository.findById(dto.categoryId())
		                                      .orElseThrow(() -> new RuntimeException(
			                                      "Category not found with id: " + dto.categoryId()));

		budget.setUser(user);
		budget.setCategory(category);
		budget.setAmount(dto.amount());
		budget.setStartDate(dto.startDate());
		budget.setEndDate(dto.endDate());
		budget.setFrequency(dto.frequency());
		budget.setType(dto.type());
		budget.setActive(true);

		return budget;
	}

	@Transactional
	public BudgetResponseDTO createBudget(BudgetRequestDTO dto) {
		// Validate input
		if (dto == null || dto.amount() == null || dto.amount().signum() <= 0) {
			throw new IllegalArgumentException("Budget amount must be greater than zero");
		}

		if (dto.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (dto.categoryId() == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}

		if (dto.startDate() == null || dto.endDate() == null) {
			throw new IllegalArgumentException("Start date and end date cannot be null");
		}

		if (dto.startDate().isAfter(dto.endDate())) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}

		Budget budget = toEntity(dto);
		Budget savedBudget = budgetRepository.save(budget);
		return toDTO(savedBudget);
	}

	@Transactional(readOnly = true)
	public BudgetResponseDTO getBudgetById(Long budgetId) {
		if (budgetId == null) {
			throw new IllegalArgumentException("Budget ID cannot be null");
		}

		Budget budget = budgetRepository.findById(budgetId)
		                                .orElseThrow(() -> new RuntimeException(
			                                "Budget not found with id: " + budgetId));

		return toDTO(budget);
	}

	@Transactional(readOnly = true)
	public List<BudgetResponseDTO> getBudgetsByUser(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		// Verify user exists
		usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

		return budgetRepository.findActiveBudgetsForUserAndDate(userId, LocalDate.now())
		                       .stream()
		                       .map(this::toDTO)
		                       .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<BudgetResponseDTO> getBudgetsByUserAndDate(UUID userId, LocalDate date) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (date == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}

		// Verify user exists
		usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

		return budgetRepository.findActiveBudgetsForUserAndDate(userId, date)
		                       .stream()
		                       .map(this::toDTO)
		                       .collect(Collectors.toList());
	}

	@Transactional
	public BudgetResponseDTO updateBudget(Long budgetId, BudgetRequestDTO dto) {
		if (budgetId == null) {
			throw new IllegalArgumentException("Budget ID cannot be null");
		}

		Budget existingBudget = budgetRepository.findById(budgetId)
		                                        .orElseThrow(() -> new RuntimeException(
			                                        "Budget not found with id: " + budgetId));

		// Verify user owns this budget
		if (!existingBudget.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("User does not have permission to update this budget");
		}

		// Validate input
		if (dto.amount() == null || dto.amount().signum() <= 0) {
			throw new IllegalArgumentException("Budget amount must be greater than zero");
		}

		if (dto.startDate().isAfter(dto.endDate())) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}

		existingBudget.setAmount(dto.amount());
		existingBudget.setStartDate(dto.startDate());
		existingBudget.setEndDate(dto.endDate());
		existingBudget.setFrequency(dto.frequency());
		existingBudget.setType(dto.type());

		Budget updatedBudget = budgetRepository.save(existingBudget);
		return toDTO(updatedBudget);
	}

	@Transactional
	public void deleteBudget(Long budgetId, UUID userId) {
		if (budgetId == null) {
			throw new IllegalArgumentException("Budget ID cannot be null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Budget budget = budgetRepository.findById(budgetId)
		                                .orElseThrow(() -> new RuntimeException(
			                                "Budget not found with id: " + budgetId));

		// Verify user owns this budget
		if (!budget.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to delete this budget");
		}

		budgetRepository.deleteById(budgetId);
	}

	@Transactional
	public BudgetResponseDTO deactivateBudget(Long budgetId, UUID userId) {
		if (budgetId == null) {
			throw new IllegalArgumentException("Budget ID cannot be null");
		}

		Budget budget = budgetRepository.findById(budgetId)
		                                .orElseThrow(() -> new RuntimeException(
			                                "Budget not found with id: " + budgetId));

		// Verify user owns this budget
		if (!budget.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to deactivate this budget");
		}

		budget.setActive(false);
		Budget updatedBudget = budgetRepository.save(budget);
		return toDTO(updatedBudget);
	}

	@Transactional(readOnly = true)
	public List<BudgetResponseDTO> getAllBudgets() {
		return budgetRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
	}
}
