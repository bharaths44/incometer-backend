package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UsersRepository userRepository;

	public AuthService(UsersRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Users getAuthenticatedUser(Jwt jwt) {
		String email = jwt.getSubject(); // Assuming subject is the email
		return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
	}
}
