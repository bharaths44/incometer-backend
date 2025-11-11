package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.service.TwilioService;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppMessageProcessor {

	private final UsersRepository usersRepository;
	private final TransactionMessageHandler transactionHandler;
	private final TwilioService twilioService;

	@Value("${app.registration-url}")
	private String registrationUrl;

	public WhatsAppMessageProcessor(UsersRepository usersRepository,
	                                TransactionMessageHandler transactionHandler,
	                                TwilioService twilioService) {
		this.usersRepository = usersRepository;
		this.transactionHandler = transactionHandler;
		this.twilioService = twilioService;
	}
	public void processMessage(String from, String body) {
		body = body.trim();

		Users user = usersRepository.findByPhoneNumber(from);
		if (user == null) {
			String message = "❌ You are not registered. Please register at " + registrationUrl;
			twilioService.sendWhatsAppMessage(from, message);
			return;
		}
		WhatsAppMessageRequest reply = transactionHandler.handleTransactionMessage(user, body);
		twilioService.sendWhatsAppMessage(from, reply);
	}
}
