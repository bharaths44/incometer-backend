package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.entities.Users;
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
public class TransactionRepositoryTest {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private PaymentMethodRepository paymentMethodRepository;

	private Users testUser;
	private Category testCategory;
	private PaymentMethod testPaymentMethod;

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

		// Create and save test payment method
		testPaymentMethod = new PaymentMethod();
		testPaymentMethod.setName("Cash");
		testPaymentMethod.setType(com.bharath.incometer.enums.PaymentType.CASH);
		testPaymentMethod.setUser(testUser);
		testPaymentMethod = paymentMethodRepository.save(testPaymentMethod);
	}

	@Test
	void shouldSaveAndFindTransaction() {
		// Given
		Transaction transaction = createTransaction(BigDecimal.valueOf(50.00),
		                                            TransactionType.EXPENSE,
		                                            LocalDate.now());

		// When
		Transaction saved = transactionRepository.save(transaction);

		// Then
		assertNotNull(saved.getTransactionId());
		assertEquals(testUser.getUserId(), saved.getUser().getUserId());
		assertEquals(BigDecimal.valueOf(50.00), saved.getAmount());
		assertEquals(TransactionType.EXPENSE, saved.getTransactionType());
	}

	@Test
	void shouldFindTransactionsByUserId() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(25.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(75.00),
		                                             TransactionType.INCOME,
		                                             LocalDate.now()));

		// When
		List<Transaction> transactions = transactionRepository.findByUserUserId(testUser.getUserId());

		// Then
		assertEquals(2, transactions.size());
		assertTrue(transactions.stream().allMatch(t -> t.getUser().getUserId().equals(testUser.getUserId())));
	}

	@Test
	void shouldFindTransactionsByUserIdAndType() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(25.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(75.00),
		                                             TransactionType.INCOME,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(100.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));

		// When
		List<Transaction> expenseTransactions =
			transactionRepository.findByUserUserIdAndTransactionType(testUser.getUserId(),
			                                                         TransactionType.EXPENSE);

		// Then
		assertEquals(2, expenseTransactions.size());
		assertTrue(expenseTransactions.stream().allMatch(t -> t.getTransactionType() == TransactionType.EXPENSE));
	}

	@Test
	void shouldFindTransactionsByDateRange() {
		// Given
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		LocalDate tomorrow = today.plusDays(1);

		transactionRepository.save(createTransaction(BigDecimal.valueOf(25.00), TransactionType.EXPENSE, yesterday));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(50.00), TransactionType.EXPENSE, today));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(75.00), TransactionType.EXPENSE, tomorrow));

		// When
		List<Transaction> transactions =
			transactionRepository.findByUserUserIdAndTransactionDateBetween(testUser.getUserId(), yesterday, today);

		// Then
		assertEquals(2, transactions.size());
		assertTrue(transactions.stream()
		                       .allMatch(t -> !t.getTransactionDate().isBefore(yesterday) &&
		                                      !t.getTransactionDate().isAfter(today)));
	}

	@Test
	void shouldCalculateSumAmountByUserId() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(25.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(75.00),
		                                             TransactionType.INCOME,
		                                             LocalDate.now()));

		// When
		BigDecimal total = transactionRepository.sumAmountByUserId(testUser.getUserId());

		// Then
		assertEquals(new BigDecimal("100.00"), total);
	}

	@Test
	void shouldCalculateSumAmountByUserIdAndType() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(25.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(50.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		transactionRepository.save(createTransaction(BigDecimal.valueOf(75.00),
		                                             TransactionType.INCOME,
		                                             LocalDate.now()));

		// When
		BigDecimal expenseTotal = transactionRepository.sumAmountByUserIdAndType(testUser.getUserId(),
		                                                                         TransactionType.EXPENSE);

		// Then
		assertEquals(new BigDecimal("75.00"), expenseTotal);
	}

	@Test
	void shouldDeleteTransactionsByCategoryId() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(50.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		assertEquals(1, transactionRepository.findByUserUserId(testUser.getUserId()).size());

		// When
		transactionRepository.deleteByCategoryId(testCategory.getCategoryId());

		// Then
		assertEquals(0, transactionRepository.findByUserUserId(testUser.getUserId()).size());
	}

	@Test
	void shouldDeleteTransactionsByPaymentMethodId() {
		// Given
		transactionRepository.save(createTransaction(BigDecimal.valueOf(50.00),
		                                             TransactionType.EXPENSE,
		                                             LocalDate.now()));
		assertEquals(1, transactionRepository.findByUserUserId(testUser.getUserId()).size());

		// When
		transactionRepository.deleteByPaymentMethodPaymentMethodId(testPaymentMethod.getPaymentMethodId());

		// Then
		assertEquals(0, transactionRepository.findByUserUserId(testUser.getUserId()).size());
	}

	private Transaction createTransaction(BigDecimal amount, TransactionType type, LocalDate date) {
		Transaction transaction = new Transaction();
		transaction.setUser(testUser);
		transaction.setCategory(testCategory);
		transaction.setPaymentMethod(testPaymentMethod);
		transaction.setAmount(amount);
		transaction.setDescription("Test transaction");
		transaction.setTransactionDate(date);
		transaction.setTransactionType(type);
		return transaction;
	}
}