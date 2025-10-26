package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.Expense;
import com.bharath.incometer.entities.TransactionType;
import com.bharath.incometer.entities.Users;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class ExpenseRepositoryTest {

	@Autowired
	private ExpenseRepository expenseRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UsersRepository usersRepository;

	private Users user;
	private Category category;

	@BeforeEach
	void setUp() {
		user = createUser();
		category = createCategory(user);
	}

	private Users createUser() {
		Users u = new Users();
		u.setName("Test User");
		u.setEmail("test@example.com");
		u.setPhoneNumber("1234567890");
		u.setPassword("password");
		return usersRepository.save(u);
	}

	private Category createCategory(Users user) {
		Category c = new Category();
		c.setUser(user);
		c.setName("Food");
		c.setType(TransactionType.EXPENSE);
		return categoryRepository.save(c);
	}

	private void createExpense(Users user, Category category, BigDecimal amount, String description) {
		Expense e = new Expense();
		e.setUser(user);
		e.setCategory(category);
		e.setAmount(amount);
		e.setDescription(description);
		e.setPaymentMethod("Cash");
		e.setExpenseDate(LocalDate.now());
		expenseRepository.save(e);
	}

	@Test
	void testFindByUserUserId() {
		createExpense(user, category, BigDecimal.valueOf(50), "Lunch");

		List<Expense> result = expenseRepository.findByUserUserId(user.getUserId());
		assertEquals(1, result.size());
		assertEquals(BigDecimal.valueOf(50), result.getFirst().getAmount());
	}

	@Test
	void testExistsByCategoryCategoryId() {
		createExpense(user, category, BigDecimal.valueOf(50), "Lunch");

		boolean exists = expenseRepository.existsByCategoryCategoryId(category.getCategoryId());
		assertTrue(exists);
	}

	@Test
	void testSumAmountByUserId() {
		createExpense(user, category, BigDecimal.valueOf(50), "Lunch");
		createExpense(user, category, BigDecimal.valueOf(30), "Snack");
		BigDecimal sum = expenseRepository.sumAmountByUserId(user.getUserId());
		assertEquals(new BigDecimal("80.00"), sum);
	}
}
