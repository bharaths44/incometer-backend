package com.bharath.incometer.service;

import com.bharath.incometer.entities.TransactionType;
import com.bharath.incometer.utils.TransactionExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class GeminiExtractionService {

	private static final Logger logger = LoggerFactory.getLogger(GeminiExtractionService.class);
	private final Client geminiClient;
	private final ObjectMapper objectMapper;

	public GeminiExtractionService() {
		String apiKey = System.getenv("GEMINI_API_KEY");
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"GEMINI_API_KEY environment variable is not set. Set it in `.env` or your environment.");
		}
		this.geminiClient = Client.builder().apiKey(apiKey).build();
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * Extract transaction information from user message using Gemini AI.
	 */
	public TransactionExtractionResult extractTransaction(String body, TransactionType type) {
		try {
			String prompt = "Extract structured expense/income information from the following text:\n" + "Text: \"" + body + "\"\n" + "Return JSON with fields: amount (number), category (string), payment_method (string), date (YYYY-MM-DD). " + "For the date field, ALWAYS return in YYYY-MM-DD format. Convert relative dates like 'today', 'yesterday', 'tomorrow' to actual dates. " + "Today's date is " + LocalDate.now() + ". " + "If a field cannot be determined, leave it null. " + "Transaction type: " + type.name();

			GenerateContentResponse response = geminiClient.models.generateContent("gemini-2.0-flash",
																				   prompt,
																				   null);
			String json = response.text();
			logger.info("Gemini response: {}", json);

			// Clean the response to remove Markdown code blocks
			if (json != null && json.startsWith("```json")) {
				json = json.substring(7).trim();
			}
			if (json != null && json.endsWith("```")) {
				json = json.substring(0, json.length() - 3).trim();
			}

			logger.info("Cleaned json: {}", json);

			// Parse JSON using ObjectMapper
			@SuppressWarnings("unchecked") Map<String, Object> map = objectMapper.readValue(json, Map.class);
			logger.info("Parsed map: {}", map);

			BigDecimal amount = map.get("amount") != null ? new BigDecimal(map.get("amount").toString()) : null;
			logger.info("Amount: {}", amount);
			String category = map.getOrDefault("category", "").toString();
			String paymentMethod = map.getOrDefault("payment_method",
													type == TransactionType.EXPENSE ? "Cash" : "Bank Account")
									  .toString();
			LocalDate date = parseDate(map.get("date"));

			return new TransactionExtractionResult(amount, category, paymentMethod, date);

		} catch (Exception e) {
			logger.error("Error in Gemini extraction", e);
			return new TransactionExtractionResult(null,
												   type == TransactionType.EXPENSE ? "Miscellaneous" : "Bank Account",
												   type == TransactionType.EXPENSE ? "Cash" : "Bank Account",
												   LocalDate.now());
		}
	}

	/**
	 * Parse date from various formats including relative dates.
	 */
	private LocalDate parseDate(Object dateValue) {
		if (dateValue == null) {
			return LocalDate.now();
		}

		String dateStr = dateValue.toString().trim().toLowerCase();

		// Handle relative dates
		switch (dateStr) {
			case "today":
				return LocalDate.now();
			case "yesterday":
				return LocalDate.now().minusDays(1);
			case "tomorrow":
				return LocalDate.now().plusDays(1);
		}

		// Try to parse as ISO date (YYYY-MM-DD)
		try {
			return LocalDate.parse(dateStr);
		} catch (Exception e) {
			logger.warn("Could not parse date '{}', using current date", dateStr);
			return LocalDate.now();
		}
	}
}

