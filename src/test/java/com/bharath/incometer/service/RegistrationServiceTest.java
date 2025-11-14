package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserRequestDTO;
import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.service.auth.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private RegistrationService registrationService;

	private Users user;

	@BeforeEach
	void setUp() {

		user = new Users();
		user.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		user.setName("Test User");
		user.setEmail("1234567890@whatsapp.local");
		user.setPhoneNumber("1234567890");
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
	}

	@Test
	void testRegisterUser() {
		String phoneNumber = "1234567890";
		String name = "Test User";
		when(usersRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
		UserResponseDTO response = new UserResponseDTO(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                               "Test User",
		                                               "1234567890@whatsapp.local",
		                                               "1234567890",
		                                               user.getCreatedAt(),
		                                               user.getUpdatedAt());
		when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

		System.out.println("Input: phoneNumber='" + phoneNumber + "', name='" + name + "'");
		String expected = "✅ Registered successfully! You can now add expenses.";
		System.out.println("Expected: " + expected);

		String result = registrationService.registerUser(phoneNumber, name);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testRegisterUserAlreadyExists() {
		String phoneNumber = "1234567890";
		String name = "Test User";
		when(usersRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

		System.out.println("Input: phoneNumber='" + phoneNumber + "', name='" + name + "'");
		String expected = "❌ You are already registered.";
		System.out.println("Expected: " + expected);

		String result = registrationService.registerUser(phoneNumber, name);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
