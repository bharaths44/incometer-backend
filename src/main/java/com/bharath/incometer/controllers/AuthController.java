package com.bharath.incometer.controllers;

import com.bharath.incometer.models.LoginRequest;
import com.bharath.incometer.models.RefreshRequest;
import com.bharath.incometer.models.RegisterRequest;
import com.bharath.incometer.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationService service;

	@PostMapping("/register")
	public ResponseEntity<String> register(
		@RequestBody RegisterRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(service.register(request, response));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<String> authenticate(
		@RequestBody LoginRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(service.authenticate(request, response));
	}

	@PostMapping("/refresh")
	public ResponseEntity<String> refresh(
		@RequestBody RefreshRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(service.refresh(request, response));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response) {
		// Clear access token cookie
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
			.httpOnly(true)
			.secure(true) // Should match your cookie.secure setting
			.path("/")
			.maxAge(0) // Expire immediately
			.sameSite("Lax")
			.build();

		// Clear refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.sameSite("Lax")
			.build();

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		return ResponseEntity.ok("Logged out successfully");
	}

	// Note: Google OAuth2 is implemented via Spring Security's oauth2Login.
	// Frontend should redirect to /oauth2/authorize/google to initiate login.
	// After auth, backend redirects to frontend with JWT token in query param.
}
