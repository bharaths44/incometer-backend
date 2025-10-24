package com.example.expensetracker.service;

import com.example.expensetracker.entities.DTOs.UserRequestDTO;
import com.example.expensetracker.entities.Users;
import com.example.expensetracker.repository.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

	private final UsersRepository usersRepository;
	private final UserService userService;

	public RegistrationService(UsersRepository usersRepository, UserService userService) {
		this.usersRepository = usersRepository;
		this.userService = userService;
	}

	public String registerUser(String phoneNumber, String name) {
		if (name == null || name.trim().isEmpty()) {
			return "❌ Please provide a valid name after 'Register'.";
		}

		if (usersRepository.existsByPhoneNumber(phoneNumber)) {
			return "❌ You are already registered.";
		}

		try {
			UserRequestDTO dto = new UserRequestDTO(name.trim(),
													phoneNumber + "@whatsapp.local",
													phoneNumber,
													"whatsapp");
			userService.createUser(dto);
			return "✅ Registered successfully! You can now add expenses.";
		} catch (Exception e) {
			return "❌ Registration failed: " + e.getMessage();
		}
	}
}
