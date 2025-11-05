package com.bharath.incometer.controllers;

import com.bharath.incometer.models.AuthenticationResponse;
import com.bharath.incometer.models.LoginRequest;
import com.bharath.incometer.models.RegisterRequest;
import com.bharath.incometer.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationService service;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(
		@RequestBody RegisterRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(service.register(request, response));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(
		@RequestBody LoginRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(service.authenticate(request, response));
	}

	// Note: Google OAuth2 is implemented via Spring Security's oauth2Login.
	// Frontend should redirect to /oauth2/authorize/google to initiate login.
	// After auth, backend redirects to frontend with JWT token in query param.
}
