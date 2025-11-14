package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.models.LoginRequest;
import com.bharath.incometer.models.RegisterRequest;
import com.bharath.incometer.service.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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

	@Value("${app.cookie.secure:true}")
	private boolean cookieSecure;

	@Value("${app.cookie.sameSite:Lax}")
	private String cookieSameSite;

	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(
		@RequestBody RegisterRequest request, HttpServletResponse response) {
		System.out.println(">>> POST /api/v1/auth/register - Email: " + request.getEmail());
		UserResponseDTO result = service.register(request, response);
		System.out.println("<<< Register response: " + result);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<UserResponseDTO> authenticate(
		@RequestBody LoginRequest request, HttpServletResponse response) {
		System.out.println(">>> POST /api/v1/auth/authenticate - Email: " + request.getEmail());
		UserResponseDTO result = service.authenticate(request, response);
		System.out.println("<<< Authenticate response: " + result);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/refresh")
	public ResponseEntity<String> refresh(
		HttpServletRequest request, HttpServletResponse response) {
		System.out.println(">>> POST /api/v1/auth/refresh");
		String result = service.refresh(request, response);
		System.out.println("<<< Refresh response: " + result);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response) {
		System.out.println(">>> POST /api/v1/auth/logout");
		// Clear access token cookie
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
		                                            .httpOnly(true)
		                                            .secure(cookieSecure) // Should match your cookie.secure setting
		                                            .path("/")
		                                            .maxAge(0) // Expire immediately
		                                            .sameSite(cookieSameSite)
		                                            .build();

		// Clear refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
		                                             .httpOnly(true)
		                                             .secure(cookieSecure)
		                                             .path("/")
		                                             .maxAge(0)
		                                             .sameSite(cookieSameSite)
		                                             .build();

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		System.out.println("✓ Cookies cleared");
		System.out.println("<<< Logout response: Logged out successfully");
		return ResponseEntity.ok("Logged out successfully");
	}

	// Note: Google OAuth2 is implemented via Spring Security's oauth2Login.
	// Frontend should redirect to /oauth2/authorize/google to initiate login.
	// After auth, backend redirects to frontend with JWT token in query param.
}
