package com.bharath.incometer.service;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.enums.BudgetType;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.models.BudgetAnalytics;
import com.bharath.incometer.models.CategoryAnalytics;
import com.bharath.incometer.models.ExpenseSummary;
import com.bharath.incometer.repository.BudgetRepository;
import com.bharath.incometer.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

	private final TransactionRepository transactionRepository;
	private final BudgetRepository budgetRepository;

	public AnalyticsService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
		this.transactionRepository = transactionRepository;
		this.budgetRepository = budgetRepository;
	}

	public ExpenseSummary getExpenseSummary(UUID userId) {
		BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
		if (totalIncome == null) totalIncome = BigDecimal.ZERO;

		BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
		if (totalExpense == null) totalExpense = BigDecimal.ZERO;

		BigDecimal netSavings = totalIncome.subtract(totalExpense);

		LocalDate now = LocalDate.now();
		LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

		BigDecimal currentMonthExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId,
		                                                                                            TransactionType.EXPENSE,
		                                                                                            startOfMonth,
		                                                                                            endOfMonth);
		if (currentMonthExpense == null) currentMonthExpense = BigDecimal.ZERO;

		BigDecimal currentMonthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId,
		                                                                                           TransactionType.INCOME,
		                                                                                           startOfMonth,
		                                                                                           endOfMonth);
		if (currentMonthIncome == null) currentMonthIncome = BigDecimal.ZERO;

		// Calculate previous month values
		LocalDate previousMonth = now.minusMonths(1);
		LocalDate previousMonthStart = previousMonth.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate previousMonthEnd = previousMonth.with(TemporalAdjusters.lastDayOfMonth());

		BigDecimal previousMonthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId,
		                                                                                            TransactionType.INCOME,
		                                                                                            previousMonthStart,
		                                                                                            previousMonthEnd);
		if (previousMonthIncome == null) previousMonthIncome = BigDecimal.ZERO;

		BigDecimal previousMonthExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId,
		                                                                                             TransactionType.EXPENSE,
		                                                                                             previousMonthStart,
		                                                                                             previousMonthEnd);
		if (previousMonthExpense == null) previousMonthExpense = BigDecimal.ZERO;

		BigDecimal currentSavings = currentMonthIncome.subtract(currentMonthExpense);
		BigDecimal previousSavings = previousMonthIncome.subtract(previousMonthExpense);

		// Calculate percentage changes
		BigDecimal incomePercentageChange = calculatePercentageChange(currentMonthIncome, previousMonthIncome);
		BigDecimal expensePercentageChange = calculatePercentageChange(currentMonthExpense, previousMonthExpense);
		BigDecimal savingsPercentageChange = calculatePercentageChange(currentSavings, previousSavings);

		return new ExpenseSummary(totalIncome,
		                          totalExpense,
		                          netSavings,
		                          currentMonthExpense,
		                          currentMonthIncome,
		                          incomePercentageChange,
		                          expensePercentageChange,
		                          savingsPercentageChange);
	}

	public List<CategoryAnalytics> getCategoryAnalytics(UUID userId) {
		List<Transaction> expenses = transactionRepository.findByUserUserIdAndTransactionType(userId,
		                                                                                      TransactionType.EXPENSE);
		BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
		if (totalExpense == null || totalExpense.equals(BigDecimal.ZERO)) {
			return Collections.emptyList();
		}

		Map<String, BigDecimal> spendingByCategory = expenses.stream()
		                                                     .collect(Collectors.groupingBy(expense -> expense.getCategory()
		                                                                                                      .getName(),
		                                                                                    Collectors.reducing(
			                                                                                    BigDecimal.ZERO,
			                                                                                    Transaction::getAmount,
			                                                                                    BigDecimal::add)));

		return spendingByCategory.entrySet().stream().map(entry -> {
			String categoryName = entry.getKey();
			BigDecimal totalSpent = entry.getValue();
			BigDecimal percentage = totalSpent.divide(totalExpense, 4, RoundingMode.HALF_UP)
			                                  .multiply(BigDecimal.valueOf(100));
			return new CategoryAnalytics(categoryName, totalSpent, percentage);
		}).collect(Collectors.toList());
	}

	public List<BudgetAnalytics> getBudgetAnalytics(UUID userId) {
		LocalDate currentDate = LocalDate.now();
		List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForUserAndDate(userId, currentDate);

		return activeBudgets.stream()
		                    .map(budget -> {
			                    String categoryName = budget.getCategory()
			                                                .getName();
			                    BigDecimal amount = budget.getAmount();
			                    BudgetType type = budget.getType();

			                    BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryIdAndDateRange(
				                    userId,
				                    budget.getCategory()
				                          .getCategoryId(),
				                    budget.getStartDate(),
				                    budget.getEndDate());
			                    if (spent == null) spent = BigDecimal.ZERO;

			                    BigDecimal remaining = amount.subtract(spent);
			                    double percentage = 0.0;
			                    boolean exceeded = false;
			                    double percentage1 = spent.divide(amount, 4, RoundingMode.HALF_UP)
			                                              .multiply(BigDecimal.valueOf(100))
			                                              .doubleValue();
			                    if (type == BudgetType.LIMIT) {
				                    percentage = percentage1;
				                    exceeded = spent.compareTo(amount) > 0;
			                    } else if (type == BudgetType.TARGET) {
				                    percentage = percentage1;
			                    }

			                    return new BudgetAnalytics(categoryName,
			                                               spent,
			                                               amount,
			                                               type,
			                                               remaining,
			                                               percentage,
			                                               exceeded);
		                    })
		                    .collect(Collectors.toList());
	}

	private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
		if (previous.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		BigDecimal change = current.subtract(previous);
		BigDecimal percentage = change.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
		return percentage.setScale(2, RoundingMode.HALF_UP);
	}
}
