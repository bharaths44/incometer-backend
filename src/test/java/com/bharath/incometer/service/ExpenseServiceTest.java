package com.bharath.incometer.service;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.ExpenseRequestDTO;
import com.bharath.incometer.entities.DTOs.ExpenseResponseDTO;
import com.bharath.incometer.entities.Expense;
import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.PaymentType;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.ExpenseRepository;
import com.bharath.incometer.repository.PaymentMethodRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

	@Mock
	private ExpenseRepository expenseRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@InjectMocks
	private ExpenseService expenseService;

	private Users user;
	private Category category;
	private Expense expense;
	private PaymentMethod paymentMethod;

	@BeforeEach
	void setUp() {

		user = new Users();
		user.setUserId(1L);
		user.setName("Test User");

		category = new Category();
		category.setCategoryId(1L);
		category.setUser(user);
		category.setName("Food");
		category.setType(TransactionType.EXPENSE);

		paymentMethod = new PaymentMethod();
		paymentMethod.setPaymentMethodId(1L);
		paymentMethod.setName("Cash");
		paymentMethod.setType(PaymentType.CASH);
		paymentMethod.setDisplayName("Cash");

		expense = new Expense();
		expense.setExpenseId(1L);
		expense.setUser(user);
		expense.setCategory(category);
		expense.setAmount(BigDecimal.valueOf(50.00));
		expense.setDescription("Lunch");
		expense.setPaymentMethod(paymentMethod);
		expense.setExpenseDate(LocalDate.now());
		expense.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void testCreateExpense() {
		ExpenseRequestDTO request = new ExpenseRequestDTO(1L,
		                                                  1L,
		                                                  BigDecimal.valueOf(50.00),
		                                                  "Lunch",
		                                                  1L,
		                                                  LocalDate.now());
		when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
		when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

		System.out.println("Input: " + request);
		ExpenseResponseDTO expected = new ExpenseResponseDTO(1L,
		                                                     1L,
		                                                     new ExpenseResponseDTO.CategoryDto(1L, "Food", null),
		                                                     BigDecimal.valueOf(50.00),
		                                                     "Lunch",
		                                                     new ExpenseResponseDTO.PaymentMethodDto(1L,
		                                                                                             "Cash",
		                                                                                             "Cash",
		                                                                                             "CASH"),
		                                                     LocalDate.now());
		System.out.println("Expected: " + expected);

		ExpenseResponseDTO result = expenseService.createExpense(request);
		System.out.println("Real Output: " + result);

		assertEquals(expected.expenseId(), result.expenseId());
		assertEquals(expected.amount(), result.amount());
		assertEquals(expected.description(), result.description());
	}

	@Test
	void testGetExpensesByUserId() {
		when(expenseRepository.findByUserUserId(1L)).thenReturn(Collections.singletonList(expense));

		System.out.println("Input: userId=1L");
		List<ExpenseResponseDTO> expected = List.of(new ExpenseResponseDTO(1L,
		                                                                   1L,
		                                                                   new ExpenseResponseDTO.CategoryDto(1L,
		                                                                                                      "Food",
		                                                                                                      null),
		                                                                   BigDecimal.valueOf(50.00),
		                                                                   "Lunch",
		                                                                   new ExpenseResponseDTO.PaymentMethodDto(1L,
		                                                                                                           "Cash",
		                                                                                                           "Cash",
		                                                                                                           "CASH"),
		                                                                   LocalDate.now()));
		System.out.println("Expected: " + expected);

		List<ExpenseResponseDTO> result = expenseService.getExpensesByUserId(1L);
		System.out.println("Real Output: " + result);

		assertEquals(1, result.size());
		assertEquals(BigDecimal.valueOf(50.00),
		             result.getFirst()
		                   .amount());
	}

	@Test
	void testGetTotalExpensesByUserId() {
		when(expenseRepository.sumAmountByUserId(1L)).thenReturn(BigDecimal.valueOf(100.00));

		System.out.println("Input: userId=1L");
		BigDecimal expected = BigDecimal.valueOf(100.00);
		System.out.println("Expected: " + expected);

		BigDecimal result = expenseService.getTotalExpensesByUserId(1L);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
