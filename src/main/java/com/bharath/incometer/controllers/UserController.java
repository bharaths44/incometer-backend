package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.UserRequestDTO;
import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.entities.DTOs.UserStatsResponseDTO;
import com.bharath.incometer.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<UserResponseDTO> createUser(
		@RequestBody UserRequestDTO userRequestDTO) {
		try {
			UserResponseDTO createdUser = userService.createUser(userRequestDTO);
			return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error creating user: " + e.getMessage());
		}
	}


	@GetMapping("/{userId}")
	public ResponseEntity<UserResponseDTO> getUserById(
		@PathVariable UUID userId) {
		try {
			UserResponseDTO user = userService.getUserById(userId);
			return ResponseEntity.ok(user);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving user: " + e.getMessage());
		}
	}


	@PutMapping("/{userId}")
	public ResponseEntity<UserResponseDTO> updateUser(
		@PathVariable UUID userId,
		@RequestBody UserRequestDTO userRequestDTO) {
		try {
			UserResponseDTO updatedUser = userService.updateUser(userId, userRequestDTO);
			return ResponseEntity.ok(updatedUser);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error updating user: " + e.getMessage());
		}
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(
		@PathVariable UUID userId) {
		try {
			userService.deleteUser(userId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error deleting user: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
		try {
			List<UserResponseDTO> users = userService.getAllUsers();
			return ResponseEntity.ok(users);
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving users: " + e.getMessage());
		}
	}


	@GetMapping("/{userId}/stats")
	public ResponseEntity<UserStatsResponseDTO> getUserStats(
		@PathVariable UUID userId) {
		try {
			UserStatsResponseDTO stats = userService.getUserStats(userId);
			return ResponseEntity.ok(stats);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving user stats: " + e.getMessage());
		}
	}


	@GetMapping("/stats")
	public ResponseEntity<List<UserStatsResponseDTO>> getAllUserStats() {
		try {
			List<UserStatsResponseDTO> stats = userService.getAllUserStats();
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving all user stats: " + e.getMessage());
		}
	}
}
