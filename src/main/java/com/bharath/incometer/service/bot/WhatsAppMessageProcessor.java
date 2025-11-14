package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageProcessor {

	private final UsersRepository usersRepository;
	private final WhatsAppCommandHandler commandHandler;


	public WhatsAppMessageProcessor(UsersRepository usersRepository, WhatsAppCommandHandler commandHandler) {
		this.usersRepository = usersRepository;
		this.commandHandler = commandHandler;
	}

	public void processMessage(String from, String body) {
		String normalizedBody = (body == null ? "" : body.trim());
		String lowerBody = normalizedBody.toLowerCase();

		Users user = usersRepository.findByPhoneNumber(from);
		if (user == null) {
			commandHandler.handleUnregisteredUser(from);
			return;
		}

		// Handle commands
		switch (lowerBody) {
			case "hello", "hi" -> commandHandler.handleGreetings(from);
			case "help" -> commandHandler.handleHelp(from);
			case "balance" -> commandHandler.handleBalance(from, user);
			case "list transactions" -> commandHandler.handleListTransactions(from, user);
			default -> {
				if (lowerBody.startsWith("summary")) {
					commandHandler.handleSummary(from, normalizedBody, user);
				} else if (lowerBody.startsWith("category summary")) {
					commandHandler.handleCategorySummary(from, normalizedBody, user);
				} else {
					commandHandler.handleDefaultTransaction(from, user, normalizedBody);
				}
			}
		}
	}
}
