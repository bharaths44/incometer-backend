package com.bharath.incometer.service;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.DTOs.TransactionResponseDTO;
import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.PaymentMethodRepository;
import com.bharath.incometer.repository.TransactionRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@InjectMocks
	private TransactionService transactionService;

	private Users testUser;
	private Category testCategory;
	private PaymentMethod testPaymentMethod;
	private Transaction testTransaction;
	private TransactionRequestDTO testRequestDTO;

	@BeforeEach
	void setUp() {
		testUser = new Users();
		testUser.setUserId(UUID.randomUUID());
		testUser.setEmail("test@example.com");
		testUser.setName("Test User");

		testCategory = new Category();
		testCategory.setCategoryId(1L);
		testCategory.setName("Food");
		testCategory.setType(TransactionType.EXPENSE);
		testCategory.setUser(testUser);

		testPaymentMethod = new PaymentMethod();
		testPaymentMethod.setPaymentMethodId(1L);
		testPaymentMethod.setName("Cash");
		testPaymentMethod.setType(com.bharath.incometer.enums.PaymentType.CASH);
		testPaymentMethod.setUser(testUser);

		testTransaction = new Transaction();
		testTransaction.setTransactionId(1L);
		testTransaction.setUser(testUser);
		testTransaction.setCategory(testCategory);
		testTransaction.setPaymentMethod(testPaymentMethod);
		testTransaction.setAmount(BigDecimal.valueOf(50.00));
		testTransaction.setDescription("Lunch");
		testTransaction.setTransactionDate(LocalDate.now());
		testTransaction.setTransactionType(TransactionType.EXPENSE);

		testRequestDTO = new TransactionRequestDTO(testUser.getUserId(),
		                                           testCategory.getCategoryId(),
		                                           BigDecimal.valueOf(50.00),
		                                           "Lunch",
		                                           testPaymentMethod.getPaymentMethodId(),
		                                           LocalDate.now(),
		                                           TransactionType.EXPENSE);
	}

	@Test
	void shouldCreateTransactionSuccessfully() {
		// Given
		when(usersRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
		when(categoryRepository.findById(testCategory.getCategoryId())).thenReturn(Optional.of(testCategory));
		when(paymentMethodRepository.findById(testPaymentMethod.getPaymentMethodId())).thenReturn(Optional.of(
			testPaymentMethod));
		when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

		// When
		TransactionResponseDTO result = transactionService.createTransaction(testRequestDTO);

		// Then
		assertNotNull(result);
		assertEquals(testTransaction.getTransactionId(), result.transactionId());
		assertEquals(testUser.getUserId(), result.userUserId());
		assertEquals(BigDecimal.valueOf(50.00), result.amount());
		assertEquals("Lunch", result.description());
	}

	@Test
	void shouldGetTransactionById() {
		// Given
		when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

		// When
		TransactionResponseDTO result = transactionService.getTransactionById(1L, testUser.getUserId());

		// Then
		assertNotNull(result);
		assertEquals(1L, result.transactionId());
		assertEquals(testUser.getUserId(), result.userUserId());
	}

	@Test
	void shouldGetTransactionsByUserId() {
		// Given
		when(transactionRepository.findByUserUserId(testUser.getUserId())).thenReturn(List.of(testTransaction));

		// When
		List<TransactionResponseDTO> results = transactionService.getTransactionsByUserId(testUser.getUserId());

		// Then
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(testTransaction.getTransactionId(), results.getFirst().transactionId());
	}

	@Test
	void shouldGetTransactionsByUserIdAndType() {
		// Given
		when(transactionRepository.findByUserUserIdAndTransactionType(testUser.getUserId(),
		                                                              TransactionType.EXPENSE)).thenReturn(List.of(
			testTransaction));

		// When
		List<TransactionResponseDTO> results = transactionService.getTransactionsByUserIdAndType(testUser.getUserId(),
		                                                                                         TransactionType.EXPENSE);

		// Then
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(TransactionType.EXPENSE, testTransaction.getTransactionType());
	}

	@Test
	void shouldThrowExceptionForNullUserId() {
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionsByUserId(null));
	}

	@Test
	void shouldThrowExceptionForNullTransactionId() {
		// When & Then
		assertThrows(IllegalArgumentException.class,
		             () -> transactionService.getTransactionById(null, testUser.getUserId()));
	}

	@Test
	void shouldThrowExceptionForInvalidAmount() {
		// Given
		TransactionRequestDTO invalidRequest = new TransactionRequestDTO(testUser.getUserId(),
		                                                                 testCategory.getCategoryId(),
		                                                                 BigDecimal.ZERO,
		                                                                 // Invalid amount
		                                                                 "Test",
		                                                                 testPaymentMethod.getPaymentMethodId(),
		                                                                 LocalDate.now(),
		                                                                 TransactionType.EXPENSE);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(invalidRequest));
	}

	@Test
	void shouldThrowExceptionForFutureDate() {
		// Given
		TransactionRequestDTO invalidRequest = new TransactionRequestDTO(testUser.getUserId(),
		                                                                 testCategory.getCategoryId(),
		                                                                 BigDecimal.valueOf(50.00),
		                                                                 "Test",
		                                                                 testPaymentMethod.getPaymentMethodId(),
		                                                                 LocalDate.now().plusDays(1),
		                                                                 // Future date
		                                                                 TransactionType.EXPENSE);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(invalidRequest));
	}
}