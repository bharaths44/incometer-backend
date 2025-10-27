package com.bharath.incometer.service;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.entities.Expense;
import com.bharath.incometer.models.BudgetAnalytics;
import com.bharath.incometer.models.CategoryAnalytics;
import com.bharath.incometer.models.ExpenseSummary;
import com.bharath.incometer.repository.BudgetRepository;
import com.bharath.incometer.repository.ExpenseRepository;
import com.bharath.incometer.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

	private final IncomeRepository incomeRepository;
	private final ExpenseRepository expenseRepository;
	private final BudgetRepository budgetRepository;

	public AnalyticsService(IncomeRepository incomeRepository,
	                        ExpenseRepository expenseRepository,
	                        BudgetRepository budgetRepository) {
		this.incomeRepository = incomeRepository;
		this.expenseRepository = expenseRepository;
		this.budgetRepository = budgetRepository;
	}

	public ExpenseSummary getExpenseSummary(Long userId) {
		BigDecimal totalIncome = incomeRepository.sumAmountByUserId(userId);
		if (totalIncome == null) totalIncome = BigDecimal.ZERO;

		BigDecimal totalExpense = expenseRepository.sumAmountByUserId(userId);
		if (totalExpense == null) totalExpense = BigDecimal.ZERO;

		BigDecimal netSavings = totalIncome.subtract(totalExpense);

		LocalDate now = LocalDate.now();
		LocalDate startOfMonth = now.withDayOfMonth(1);
		LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

		BigDecimal currentMonthExpense = expenseRepository.sumAmountByUserIdAndDateRange(userId,
		                                                                                 startOfMonth,
		                                                                                 endOfMonth);
		if (currentMonthExpense == null) currentMonthExpense = BigDecimal.ZERO;

		BigDecimal currentMonthIncome = incomeRepository.sumAmountByUserIdAndDateRange(userId,
		                                                                               startOfMonth,
		                                                                               endOfMonth);
		if (currentMonthIncome == null) currentMonthIncome = BigDecimal.ZERO;

		return new ExpenseSummary(totalIncome, totalExpense, netSavings, currentMonthExpense, currentMonthIncome);
	}

	public List<CategoryAnalytics> getCategoryAnalytics(Long userId) {
		List<Expense> expenses = expenseRepository.findByUserUserId(userId);
		BigDecimal totalExpense = expenseRepository.sumAmountByUserId(userId);
		if (totalExpense == null || totalExpense.equals(BigDecimal.ZERO)) {
			return Collections.emptyList();
		}

		Map<String, BigDecimal> spendingByCategory = expenses.stream()
		                                                     .collect(Collectors.groupingBy(
			                                                     expense -> expense.getCategory()
			                                                                       .getName(),
			                                                     Collectors.reducing(BigDecimal.ZERO,
			                                                                         Expense::getAmount,
			                                                                         BigDecimal::add)
		                                                                                   ));

		return spendingByCategory.entrySet()
		                         .stream()
		                         .map(entry -> {
			                         String categoryName = entry.getKey();
			                         BigDecimal totalSpent = entry.getValue();
			                         BigDecimal percentage = totalSpent.divide(totalExpense, 4,
			                                                                   RoundingMode.HALF_UP)
			                                                           .multiply(BigDecimal.valueOf(100));
			                         return new CategoryAnalytics(categoryName, totalSpent, percentage);
		                         })
		                         .collect(Collectors.toList());
	}

	public List<BudgetAnalytics> getBudgetAnalytics(Long userId) {
		LocalDate currentDate = LocalDate.now();
		List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForUserAndDate(userId, currentDate);

		return activeBudgets.stream()
		                    .map(budget -> {
			                    String categoryName = budget.getCategory()
			                                                .getName();
			                    BigDecimal limit = budget.getAmountLimit();

			                    BigDecimal spent = expenseRepository.sumAmountByUserIdAndCategoryIdAndDateRange(
				                    userId,
				                    budget.getCategory()
				                          .getCategoryId(),
				                    budget.getStartDate(),
				                    budget.getEndDate());
			                    if (spent == null) spent = BigDecimal.ZERO;

			                    BigDecimal remaining = limit.subtract(spent);
			                    double usagePercentage = spent.divide(limit, 4, RoundingMode.HALF_UP)
			                                                  .multiply(BigDecimal.valueOf(100))
			                                                  .doubleValue();
			                    boolean exceeded = spent.compareTo(limit) > 0;

			                    return new BudgetAnalytics(categoryName,
			                                               spent,
			                                               limit,
			                                               remaining,
			                                               usagePercentage,
			                                               exceeded);
		                    })
		                    .collect(Collectors.toList());
	}
}
