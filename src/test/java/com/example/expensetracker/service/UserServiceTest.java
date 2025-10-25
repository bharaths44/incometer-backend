package com.example.expensetracker.service;

import com.example.expensetracker.entities.DTOs.UserRequestDTO;
import com.example.expensetracker.entities.DTOs.UserResponseDTO;
import com.example.expensetracker.entities.Users;
import com.example.expensetracker.repository.UsersRepository;
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
		user.setUserId(1L);
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("1234567890");
		user.setPassword("password");
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
	}

	@Test
	void testCreateUser() {
		UserRequestDTO request = new UserRequestDTO("Test User", "test@example.com", "1234567890", "password");
		when(usersRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(usersRepository.save(any(Users.class))).thenReturn(user);

		System.out.println("Input: " + request);
		UserResponseDTO expected = new UserResponseDTO(1L,
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
		when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

		System.out.println("Input: userId=1L");
		UserResponseDTO expected = new UserResponseDTO(1L,
													   "Test User",
													   "test@example.com",
													   "1234567890",
													   user.getCreatedAt(),
													   user.getUpdatedAt());
		System.out.println("Expected: " + expected);

		UserResponseDTO result = userService.getUserById(1L);
		System.out.println("Real Output: " + result);

		assertEquals(expected.userId(), result.userId());
		assertEquals(expected.name(), result.name());
	}

	@Test
	void testGetAllUsers() {
		when(usersRepository.findAll()).thenReturn(Collections.singletonList(user));

		System.out.println("Input: none");
		List<UserResponseDTO> expected = List.of(new UserResponseDTO(1L,
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
