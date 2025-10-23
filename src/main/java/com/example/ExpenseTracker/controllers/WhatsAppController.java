package com.example.ExpenseTracker.controllers;

import com.example.ExpenseTracker.service.WhatsAppMessageProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

	private final WhatsAppMessageProcessor messageProcessor;

	public WhatsAppController(WhatsAppMessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}

	@PostMapping("/webhook")
	public ResponseEntity<String> receiveMessage(
			@RequestParam("From") String from,
			@RequestParam("Body") String body
												) {
		// Trim "whatsapp:" prefix and only use phone number
		String phoneNumber = from.startsWith("whatsapp:") ? from.substring(9) : from;

		// Delegate processing to the processor
		System.out.println("Original From: " + from);
		System.out.println("Phone Number: " + phoneNumber);
		System.out.println("Body: " + body);

		String reply = messageProcessor.processMessage(phoneNumber, body);
		return ResponseEntity.ok(reply);
	}

}
