package com.bharath.incometer.models;

import lombok.Getter;

import java.util.Map;

@Getter
public class WhatsAppMessageRequest {
	// Getters
	private final String type; // "text" or "template"
	private String text;
	private String contentSid;
	private Map<String, String> variables;

	public WhatsAppMessageRequest(String text) {
		this.type = "text";
		this.text = text;
	}

	public WhatsAppMessageRequest(String contentSid, Map<String, String> variables) {
		this.type = "template";
		this.contentSid = contentSid;
		this.variables = variables;
	}

}
