package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.service.bot.TransactionMessageHandler;
import com.bharath.incometer.service.bot.WhatsAppMessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WhatsAppMessageProcessorTest {

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private RegistrationService registrationService;

	@Mock
	private TransactionMessageHandler transactionHandler;

	@InjectMocks
	private WhatsAppMessageProcessor whatsAppMessageProcessor;

	private Users user;

	@BeforeEach
	void setUp() {

		user = new Users();
		user.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		user.setName("Test User");
		user.setPhoneNumber("1234567890");
	}

	@Test
	void testProcessMessageRegister() {
		String from = "1234567890";
		String body = "register Test User";
		when(registrationService.registerUser(from, "Test User")).thenReturn("✅ Registered successfully!");

		System.out.println("Input: from='" + from + "', body='" + body + "'");
		String expected = "✅ Registered successfully!";
		System.out.println("Expected: " + expected);

		String result = whatsAppMessageProcessor.processMessage(from, body);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testProcessMessageNotRegistered() {
		String from = "1234567890";
		String body = "expense 50 food";
		when(usersRepository.findByPhoneNumber(from)).thenReturn(null);

		System.out.println("Input: from='" + from + "', body='" + body + "'");
		String expected = "❌ You are not registered. Send 'Register <Your Name>' to register.";
		System.out.println("Expected: " + expected);

		String result = whatsAppMessageProcessor.processMessage(from, body);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testProcessMessageTransaction() {
		String from = "1234567890";
		String body = "expense 50 food";
		when(usersRepository.findByPhoneNumber(from)).thenReturn(user);
		when(transactionHandler.handleTransactionMessage(user, body)).thenReturn("✅ Recorded");

		System.out.println("Input: from='" + from + "', body='" + body + "'");
		String expected = "✅ Recorded";
		System.out.println("Expected: " + expected);

		String result = whatsAppMessageProcessor.processMessage(from, body);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
