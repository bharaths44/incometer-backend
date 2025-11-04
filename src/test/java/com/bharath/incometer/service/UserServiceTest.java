package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserRequestDTO;
import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UsersRepository usersRepository;

	@InjectMocks
	private UserService userService;

	private Users user;

	@BeforeEach
	void setUp() {

		user = new Users();
		user.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("1234567890");

		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
	}

	@Test
	void testCreateUser() {
		UserRequestDTO request = new UserRequestDTO("Test User", "test@example.com", "1234567890", "password");
		when(usersRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(usersRepository.save(any(Users.class))).thenReturn(user);

		System.out.println("Input: " + request);
		UserResponseDTO expected = new UserResponseDTO(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                               "Test User",
		                                               "test@example.com",
		                                               "1234567890",
		                                               user.getCreatedAt(),
		                                               user.getUpdatedAt());
		System.out.println("Expected: " + expected);

		UserResponseDTO result = userService.createUser(request);
		System.out.println("Real Output: " + result);

		assertEquals(expected.userId(), result.userId());
		assertEquals(expected.name(), result.name());
		assertEquals(expected.email(), result.email());
	}

	@Test
	void testGetUserById() {
		when(usersRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(
			user));

		System.out.println("Input: userId=1L");
		UserResponseDTO expected = new UserResponseDTO(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                               "Test User",
		                                               "test@example.com",
		                                               "1234567890",
		                                               user.getCreatedAt(),
		                                               user.getUpdatedAt());
		System.out.println("Expected: " + expected);

		UserResponseDTO result = userService.getUserById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		System.out.println("Real Output: " + result);

		assertEquals(expected.userId(), result.userId());
		assertEquals(expected.name(), result.name());
	}

	@Test
	void testGetAllUsers() {
		when(usersRepository.findAll()).thenReturn(Collections.singletonList(user));

		System.out.println("Input: none");
		List<UserResponseDTO> expected = List.of(new UserResponseDTO(UUID.fromString(
			"550e8400-e29b-41d4-a716-446655440000"),
		                                                             "Test User",
		                                                             "test@example.com",
		                                                             "1234567890",
		                                                             user.getCreatedAt(),
		                                                             user.getUpdatedAt()));
		System.out.println("Expected: " + expected);

		List<UserResponseDTO> result = userService.getAllUsers();
		System.out.println("Real Output: " + result);

		assertEquals(1, result.size());
		assertEquals("Test User", result.getFirst().name());
	}
}
