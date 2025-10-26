package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageProcessor {

	private final UsersRepository usersRepository;
	private final RegistrationService registrationService;
	private final TransactionMessageHandler transactionHandler;

	public WhatsAppMessageProcessor(UsersRepository usersRepository,
									RegistrationService registrationService,
									TransactionMessageHandler transactionHandler) {
		this.usersRepository = usersRepository;
		this.registrationService = registrationService;
		this.transactionHandler = transactionHandler;
	}

	public String processMessage(String from, String body) {
		body = body.trim();

		if (body.toLowerCase().startsWith("register ")) {
			return registrationService.registerUser(from, body.substring(9).trim());
		}

		Users user = usersRepository.findByPhoneNumber(from);
		if (user == null) {
			return "❌ You are not registered. Send 'Register <Your Name>' to register.";
		}

		return transactionHandler.handleTransactionMessage(user, body);
	}
}
