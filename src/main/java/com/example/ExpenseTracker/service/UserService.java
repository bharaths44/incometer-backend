package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entities.DTOs.UserRequestDTO;
import com.example.ExpenseTracker.entities.DTOs.UserResponseDTO;
import com.example.ExpenseTracker.entities.Users;
import com.example.ExpenseTracker.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

	private final UsersRepository usersRepository;

	// For password encryption in a real application
	// Uncomment if you have Spring Security configured
	// private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserService(UsersRepository usersRepository) { // , PasswordEncoder passwordEncoder
		this.usersRepository = usersRepository;
		// this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Creates a new user
	 *
	 * @param userRequestDTO the user information
	 * @return UserResponseDTO containing the created user information
	 */
	@Transactional
	public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
		if (userRequestDTO == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		if (userRequestDTO.name() == null || userRequestDTO.name().trim().isEmpty()) {
			throw new IllegalArgumentException("User name cannot be empty");
		}

		if (userRequestDTO.email() == null || userRequestDTO.email().trim().isEmpty()) {
			throw new IllegalArgumentException("User email cannot be empty");
		}

		if (userRequestDTO.password() == null || userRequestDTO.password().trim().isEmpty()) {
			throw new IllegalArgumentException("User password cannot be empty");
		}

		// Check if email already exists
		if (usersRepository.existsByEmail(userRequestDTO.email())) {
			throw new IllegalArgumentException("Email already registered");
		}

		Users user = new Users();
		user.setName(userRequestDTO.name());
		user.setEmail(userRequestDTO.email());

		// In a real application, you would encode the password
		// user.setPassword(passwordEncoder.encode(userRequestDTO.password()));
		user.setPassword(userRequestDTO.password());

		Users savedUser = usersRepository.save(user);
		return mapToResponseDTO(savedUser);
	}


	public UserResponseDTO getUserById(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return usersRepository.findById(userId)
							  .map(this::mapToResponseDTO)
							  .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
	}


	@Transactional
	public UserResponseDTO updateUser(Long userId, UserRequestDTO userRequestDTO) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (userRequestDTO == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		Users existingUser = usersRepository.findById(userId)
											.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

		// Update user properties if provided
		if (userRequestDTO.name() != null && !userRequestDTO.name().trim().isEmpty()) {
			existingUser.setName(userRequestDTO.name());
		}

		if (userRequestDTO.email() != null && !userRequestDTO.email().trim().isEmpty()) {
			existingUser.setEmail(userRequestDTO.email());
		}

		if (userRequestDTO.password() != null && !userRequestDTO.password().trim().isEmpty()) {
			// In a real application, you would encode the password
			// existingUser.setPassword(passwordEncoder.encode(userRequestDTO.password()));
			existingUser.setPassword(userRequestDTO.password());
		}

		Users updatedUser = usersRepository.save(existingUser);
		return mapToResponseDTO(updatedUser);
	}

	@Transactional
	public void deleteUser(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (!usersRepository.existsById(userId)) {
			throw new RuntimeException("User not found with id: " + userId);
		}

		usersRepository.deleteById(userId);
	}


	public List<UserResponseDTO> getAllUsers() {
		return usersRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
	}


	private UserResponseDTO mapToResponseDTO(Users user) {
		return new UserResponseDTO(user.getUserId(),
								   user.getName(),
								   user.getEmail(),
								   user.getCreatedAt(),
								   user.getUpdatedAt());
	}
}
