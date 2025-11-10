package com.bharath.incometer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerKeepAliveService {

	private static final Logger logger = LoggerFactory.getLogger(ServerKeepAliveService.class);
	private final RestTemplate restTemplate;

	public ServerKeepAliveService() {
		this.restTemplate = new RestTemplate();
	}

	/**
	 * Scheduled task that runs every 290 seconds to ping the server
	 * This helps keep cloud instances alive by maintaining activity
	 */
	@Scheduled(fixedRate = 290000) // 290 seconds = 290000 milliseconds
	public void pingServer() {
		try {
			// Ping the health endpoint or root endpoint
			String healthUrl = "https://developing-mellisent-incometer-cae5ae7b.koyeb.app/actuator/health";

			logger.info("Performing keep-alive ping to server");

			// Try to ping the health endpoint first
			try {
				String response = restTemplate.getForObject(healthUrl, String.class);
				logger.debug("Health check response: {}", response);
			} catch (Exception e) {
				// If health endpoint fails, try the root endpoint
				logger.debug("Health endpoint not available, trying root endpoint");
				try {
					String rootUrl = "https://developing-mellisent-incometer-cae5ae7b.koyeb.app/";
					restTemplate.getForObject(rootUrl, String.class);
					logger.debug("Root endpoint response received");
				} catch (Exception ex) {
					logger.warn("Failed to ping server endpoints: {}", ex.getMessage());
				}
			}

			logger.info("Keep-alive ping completed successfully");

		} catch (Exception e) {
			logger.error("Error during keep-alive ping: {}", e.getMessage(), e);
		}
	}
}
