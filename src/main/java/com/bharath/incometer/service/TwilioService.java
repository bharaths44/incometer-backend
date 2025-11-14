package com.bharath.incometer.service;

import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TwilioService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.twilio.whatsapp.number}")
    private String twilioWhatsAppNumber;

    public void sendWhatsAppMessage(String to, String message) {
        sendWhatsAppMessage(to, new WhatsAppMessageRequest(message));
    }

    public void sendWhatsAppMessage(String to, WhatsAppMessageRequest request) {
        try {
            if ("template".equals(request.getType())) {
                Map<String, Object> contentVariables = new HashMap<>();
                if (request.getVariables() != null && !request.getVariables().isEmpty()) {
                    contentVariables.putAll(request.getVariables());
                }
                String variablesJson = objectMapper.writeValueAsString(contentVariables);
                Message.creator(
                    new PhoneNumber("whatsapp:" + to),
                    new PhoneNumber("whatsapp:" + twilioWhatsAppNumber),
                    ""
                ).setContentSid(request.getContentSid()).setContentVariables(variablesJson).create();
            } else {
                Message.creator(
                    new PhoneNumber("whatsapp:" + to),
                    new PhoneNumber("whatsapp:" + twilioWhatsAppNumber),
                    request.getText()
                ).create();
            }

            logger.info("Message sent to {}: {}", to, request.getType().equals("template") ? "template " + request.getContentSid() : request.getText());
        } catch (Exception e) {
            logger.error("Failed to send message to {}: {}", to, e.getMessage());
        }
    }
}
