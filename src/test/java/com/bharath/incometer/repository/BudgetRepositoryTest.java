package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Budget;
import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.BudgetFrequency;
import com.bharath.incometer.enums.BudgetType;
import com.bharath.incometer.enums.TransactionType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class BudgetRepositoryTest {

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	private Users testUser;
	private Category testCategory;

	@BeforeEach
	void setUp() {
		// Create and save test user
		testUser = new Users();
		testUser.setName("Test User");
		testUser.setEmail("test@example.com");
		testUser.setPhoneNumber("1234567890");
		testUser = usersRepository.save(testUser);

		// Create and save test category
		testCategory = new Category();
		testCategory.setName("Food");
		testCategory.setIcon("utensils");
		testCategory.setType(TransactionType.EXPENSE);
		testCategory.setUser(testUser);
		testCategory = categoryRepository.save(testCategory);
	}

	@Test
	void shouldSaveAndFindBudget() {
		// Given
		Budget budget = createBudget(BigDecimal.valueOf(500.00), LocalDate.now(), LocalDate.now().plusMonths(1));

		// When
		Budget saved = budgetRepository.save(budget);

		// Then
		assertNotNull(saved.getBudgetId());
		assertEquals(BigDecimal.valueOf(500.00), saved.getAmount());
		assertEquals(BudgetFrequency.MONTHLY, saved.getFrequency());
		assertTrue(saved.isActive());
		assertEquals(testUser.getUserId(), saved.getUser().getUserId());
		assertEquals(testCategory.getCategoryId(), saved.getCategory().getCategoryId());
	}

	@Test
	void shouldFindActiveBudgetsForUserAndDate() {
		// Given
		LocalDate today = LocalDate.now();
		LocalDate nextMonth = today.plusMonths(1);

		Budget activeBudget = budgetRepository.save(createBudget(BigDecimal.valueOf(500.00), today, nextMonth));
		budgetRepository.save(createInactiveBudget(BigDecimal.valueOf(300.00), today, nextMonth));

		// When
		List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForUserAndDate(testUser.getUserId(), today);

		// Then
		assertEquals(1, activeBudgets.size());
		assertEquals(activeBudget.getBudgetId(), activeBudgets.getFirst().getBudgetId());
		assertTrue(activeBudgets.getFirst().isActive());
	}

	@Test
	void shouldNotFindBudgetsOutsideDateRange() {
		// Given
		LocalDate today = LocalDate.now();
		LocalDate pastEnd = today.minusDays(1);
		LocalDate futureStart = today.plusDays(1);

		budgetRepository.save(createBudget(BigDecimal.valueOf(500.00), pastEnd.minusDays(10), pastEnd));
		budgetRepository.save(createBudget(BigDecimal.valueOf(300.00), futureStart, futureStart.plusDays(10)));

		// When
		List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForUserAndDate(testUser.getUserId(), today);

		// Then
		assertTrue(activeBudgets.isEmpty());
	}

	@Test
	void shouldDeleteBudgetsByCategoryId() {
		// Given
		budgetRepository.save(createBudget(BigDecimal.valueOf(500.00), LocalDate.now(),
		                                   LocalDate.now().plusMonths(1)));
		assertEquals(1, budgetRepository.findAll().size());

		// When
		budgetRepository.deleteByCategoryId(testCategory.getCategoryId());

		// Then
		assertEquals(0, budgetRepository.findAll().size());
	}

	@Test
	void shouldHandleMultipleActiveBudgets() {
		// Given
		LocalDate today = LocalDate.now();
		LocalDate nextMonth = today.plusMonths(1);

		Category anotherCategory = new Category();
		anotherCategory.setName("Transport");
		anotherCategory.setIcon("car");
		anotherCategory.setType(TransactionType.EXPENSE);
		anotherCategory.setUser(testUser);
		anotherCategory = categoryRepository.save(anotherCategory);

		budgetRepository.save(createBudget(BigDecimal.valueOf(500.00), today, nextMonth));
		budgetRepository.save(createBudgetForCategory(BigDecimal.valueOf(200.00), today, nextMonth, anotherCategory));

		// When
		List<Budget> activeBudgets = budgetRepository.findActiveBudgetsForUserAndDate(testUser.getUserId(), today);

		// Then
		assertEquals(2, activeBudgets.size());
		assertTrue(activeBudgets.stream().allMatch(Budget::isActive));
	}

	private Budget createBudget(BigDecimal amount, LocalDate startDate, LocalDate endDate) {
		return createBudgetForCategory(amount, startDate, endDate, testCategory);
	}

	private Budget createBudgetForCategory(BigDecimal amount, LocalDate startDate, LocalDate endDate,
	                                       Category category) {
		Budget budget = new Budget();
		budget.setUser(testUser);
		budget.setCategory(category);
		budget.setAmount(amount);
		budget.setStartDate(startDate);
		budget.setEndDate(endDate);
		budget.setFrequency(BudgetFrequency.MONTHLY);
		budget.setActive(true);
		budget.setType(BudgetType.LIMIT);
		return budget;
	}

	private Budget createInactiveBudget(BigDecimal amount, LocalDate startDate, LocalDate endDate) {
		Budget budget = createBudget(amount, startDate, endDate);
		budget.setActive(false);
		return budget;
	}
}