package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.models.LoginRequest;
import com.bharath.incometer.models.RefreshRequest;
import com.bharath.incometer.models.RegisterRequest;
import com.bharath.incometer.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

	public String register(RegisterRequest request, HttpServletResponse response) {
		var user = new Users();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);
		repository.save(user);
		token(response, user);
		return "User registered successfully";
	}

	private void token(HttpServletResponse response, Users user) {
		var jwtToken = jwtService.generateToken(user);
		jwtService.generateRefreshToken(user);
		Cookie cookie = new Cookie("token", jwtToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // Assuming HTTPS in production
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public String authenticate(LoginRequest request, HttpServletResponse response) {
		System.out.println("Authenticating user: " + request.getEmail());
		var user = repository.findByEmail(request.getEmail())
		                     .orElseThrow(() -> new RuntimeException("User not " + "found"));
		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			throw new RuntimeException("User registered via OAuth, please use OAuth to login");
		}
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
		                                                                           request.getPassword()));
		System.out.println("Authentication successful for: " + request.getEmail());
		token(response, user);
		return "User authenticated successfully";
	}

	public String refresh(RefreshRequest request, HttpServletResponse response) {
		String refreshToken = request.getRefreshToken();
		String username = jwtService.extractUsername(refreshToken);
		var user = repository.findByEmail(username)
		                     .orElseThrow(() -> new RuntimeException("User not found"));
		if (jwtService.isTokenValid(refreshToken, user)) {
			var newAccessToken = jwtService.generateToken(user);
			Cookie cookie = new Cookie("token", newAccessToken);
			cookie.setHttpOnly(true);
			cookie.setSecure(false); // Assuming HTTPS in production
			cookie.setPath("/");
			response.addCookie(cookie);
			return "Token refreshed successfully";
		} else {
			throw new RuntimeException("Invalid refresh token");
		}
	}
}
