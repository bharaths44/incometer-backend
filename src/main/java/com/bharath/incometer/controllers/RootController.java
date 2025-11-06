package com.bharath.incometer.controllers;

import com.bharath.incometer.config.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RootController {

	private final HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;

	@GetMapping("/")
	public String hello() {
		return "Welcome to Expense Tracker Application";
	}

	@GetMapping("/api/v1/auth/clear-oauth-cookies")
	public String clearOAuthCookies(HttpServletRequest request, HttpServletResponse response) {
		authRequestRepository.removeAuthorizationRequestCookies(request, response);
		return "OAuth cookies cleared successfully. You can now try the OAuth login again.";
	}
}
