package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.models.AuthenticationResponse;
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

	public AuthenticationResponse register(RegisterRequest request, HttpServletResponse response) {
		var user = new Users();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);
		repository.save(user);
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		Cookie cookie = new Cookie("token", jwtToken);
		response.addCookie(cookie);
		return AuthenticationResponse.builder()
		                             .accessToken(jwtToken)
		                             .refreshToken(refreshToken)
		                             .build();
	}

	public AuthenticationResponse authenticate(LoginRequest request, HttpServletResponse response) {
		System.out.println("Authenticating user: " + request.getEmail());
		var user = repository.findByEmail(request.getEmail())
		                     .orElseThrow(() -> new RuntimeException("User not " + "found"));
		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			throw new RuntimeException("User registered via OAuth, please use OAuth to login");
		}
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
		                                                                           request.getPassword()));
		System.out.println("Authentication successful for: " + request.getEmail());
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		Cookie cookie = new Cookie("token", jwtToken);
		response.addCookie(cookie);
		return AuthenticationResponse.builder()
		                             .accessToken(jwtToken)
		                             .refreshToken(refreshToken)
		                             .build();
	}

	public AuthenticationResponse refresh(RefreshRequest request) {
		String refreshToken = request.getRefreshToken();
		String username = jwtService.extractUsername(refreshToken);
		var user = repository.findByEmail(username)
		                     .orElseThrow(() -> new RuntimeException("User not found"));
		if (jwtService.isTokenValid(refreshToken, user)) {
			var newAccessToken = jwtService.generateToken(user);
			var newRefreshToken = jwtService.generateRefreshToken(user); // Optional: rotate refresh token
			return AuthenticationResponse.builder()
			                             .accessToken(newAccessToken)
			                             .refreshToken(newRefreshToken)
			                             .build();
		} else {
			throw new RuntimeException("Invalid refresh token");
		}
	}
}
