package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.models.LoginRequest;
import com.bharath.incometer.models.RefreshRequest;
import com.bharath.incometer.models.RegisterRequest;
import com.bharath.incometer.repository.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UsersRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Value("${app.cookie.secure:true}")
	private boolean cookieSecure;

	@Value("${app.cookie.sameSite:Lax}")
	private String cookieSameSite;

	public String register(RegisterRequest request, HttpServletResponse response) {
		System.out.println("=== REGISTER START ===");
		System.out.println("Registering user: " + request.getEmail());
		var user = new Users();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);
		repository.save(user);
		System.out.println("User saved to database with ID: " + user.getUserId());
		setAuthenticationCookies(response, user);
		System.out.println("=== REGISTER END ===\n");
		return "User registered successfully";
	}

	public String authenticate(LoginRequest request, HttpServletResponse response) {
		System.out.println("=== AUTHENTICATE START ===");
		System.out.println("Authenticating user: " + request.getEmail());
		var user = repository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not " +
		                                                                                             "found"));
		System.out.println("User found in database: " + user.getUserId());
		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			System.out.println("❌ User has no password (OAuth user)");
			throw new RuntimeException("User registered via OAuth, please use OAuth to login");
		}
		System.out.println("Validating password...");
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
		                                                                           request.getPassword()));
		System.out.println("✓ Password validated successfully for: " + request.getEmail());
		setAuthenticationCookies(response, user);
		System.out.println("=== AUTHENTICATE END ===\n");
		return "User authenticated successfully";
	}

	public String refresh(RefreshRequest request, HttpServletResponse response) {
		String refreshToken = request.getRefreshToken();
		String username = jwtService.extractUsername(refreshToken);
		var user = repository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
		if (jwtService.isTokenValid(refreshToken, user)) {
			var newAccessToken = jwtService.generateToken(user);

			// Set new access token cookie
			ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
			                                            .httpOnly(true)
			                                            .secure(cookieSecure)
			                                            .path("/")
			                                            .maxAge(24 * 60 * 60) // 24 hours
			                                            .sameSite(cookieSameSite)
			                                            .build();

			response.addHeader("Set-Cookie", accessCookie.toString());
			return "Token refreshed successfully";
		} else {
			throw new RuntimeException("Invalid refresh token");
		}
	}

	private void setAuthenticationCookies(HttpServletResponse response, Users user) {
		System.out.println("--- Setting Authentication Cookies ---");
		System.out.println("User: " + user.getEmail() + " (ID: " + user.getUserId() + ")");
		System.out.println("Cookie secure setting: " + cookieSecure);

		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);

		System.out.println("Access token generated (length: " + jwtToken.length() + ")");
		System.out.println("Refresh token generated (length: " + refreshToken.length() + ")");

		// Create access token cookie with all security attributes
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken)
		                                            .httpOnly(true)           // Cannot be accessed by JavaScript
		                                            .secure(cookieSecure)     // Only sent over HTTPS (configurable
		                                            // for dev/prod)
		                                            .path("/")                // Available for all paths
		                                            .maxAge(24 * 60 * 60)     // 24 hours
		                                            .sameSite(cookieSameSite)          // CSRF protection
		                                            .build();

		// Create refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
		                                             .httpOnly(true)
		                                             .secure(cookieSecure)
		                                             .path("/")
		                                             .maxAge(7 * 24 * 60 * 60) // 7 days
		                                             .sameSite(cookieSameSite)
		                                             .build();

		System.out.println("Access cookie: " + accessCookie);
		System.out.println("Refresh cookie: " + refreshCookie);

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		System.out.println("✓ Cookies added to response headers");
		System.out.println("--- End Setting Cookies ---");
	}
}
