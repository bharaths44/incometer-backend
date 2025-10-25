package com.example.expensetracker.service;

import com.example.expensetracker.entities.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionMessageHandlerTest {
	@Mock
	private NLPService nlpService;

	@Mock
	private ExpenseService expenseService;

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private TransactionMessageHandler transactionMessageHandler;

	private Users user;


	@BeforeEach
	void setUp() {

		user = new Users();
		user.setUserId(1L);
		user.setName("Test User");
		user.setPhoneNumber("1234567890");
	}

	@Test
	void testHandleTransactionMessageExpense() {
		String body = "expense 50 food cash";
		when(nlpService.handleExpense(any(Users.class),
									  anyString(),
									  anyMap(),
									  any(ExpenseService.class),
									  any(CategoryService.class))).thenReturn("✅ Recorded");

		System.out.println("Input: user=" + user.getPhoneNumber() + ", body='" + body + "'");
		String expected = "✅ Recorded";
		System.out.println("Expected: " + expected);

		String result = transactionMessageHandler.handleTransactionMessage(user, body);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testHandleTransactionMessageUnknown() {
		String body = "unknown message";

		System.out.println("Input: user=" + user.getPhoneNumber() + ", body='" + body + "'");
		String expected = """
						  ⚠️ Unknown command. Try:
						  - Register <Name>\
						  
						  - Expense <Amount> <Category> <Payment Method> <Optional:Date>\
						  
						  - Income <Amount> <Source> <Payment Method> <Optional:Date>""";
		System.out.println("Expected: " + expected);

		String result = transactionMessageHandler.handleTransactionMessage(user, body);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
