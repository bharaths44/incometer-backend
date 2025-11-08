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

	public String register(RegisterRequest request, HttpServletResponse response) {
		var user = new Users();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);
		repository.save(user);
		setAuthenticationCookies(response, user);
		return "User registered successfully";
	}

	public String authenticate(LoginRequest request, HttpServletResponse response) {
		System.out.println("Authenticating user: " + request.getEmail());
		var user = repository.findByEmail(request.getEmail())
		                     .orElseThrow(() -> new RuntimeException("User not found"));
		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			throw new RuntimeException("User registered via OAuth, please use OAuth to login");
		}
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
		                                                                           request.getPassword()));
		System.out.println("Authentication successful for: " + request.getEmail());
		setAuthenticationCookies(response, user);
		return "User authenticated successfully";
	}

	public String refresh(RefreshRequest request, HttpServletResponse response) {
		String refreshToken = request.getRefreshToken();
		String username = jwtService.extractUsername(refreshToken);
		var user = repository.findByEmail(username)
		                     .orElseThrow(() -> new RuntimeException("User not found"));
		if (jwtService.isTokenValid(refreshToken, user)) {
			var newAccessToken = jwtService.generateToken(user);

			// Set new access token cookie
			ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
				.httpOnly(true)
				.secure(cookieSecure)
				.path("/")
				.maxAge(24 * 60 * 60) // 24 hours
				.sameSite("Lax")
				.build();

			response.addHeader("Set-Cookie", accessCookie.toString());
			return "Token refreshed successfully";
		} else {
			throw new RuntimeException("Invalid refresh token");
		}
	}

	private void setAuthenticationCookies(HttpServletResponse response, Users user) {
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);

		// Create access token cookie with all security attributes
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken)
			.httpOnly(true)           // Cannot be accessed by JavaScript
			.secure(cookieSecure)     // Only sent over HTTPS (configurable for dev/prod)
			.path("/")                // Available for all paths
			.maxAge(24 * 60 * 60)     // 24 hours
			.sameSite("Lax")          // CSRF protection
			.build();

		// Create refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(cookieSecure)
			.path("/")
			.maxAge(7 * 24 * 60 * 60) // 7 days
			.sameSite("Lax")
			.build();

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());
	}
}

