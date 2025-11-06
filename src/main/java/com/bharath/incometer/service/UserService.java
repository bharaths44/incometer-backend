package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserRequestDTO;
import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.entities.DTOs.UserStatsResponseDTO;
import com.bharath.incometer.entities.UserStats;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UserStatsRepository;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.utils.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

	private final UsersRepository usersRepository;
	private final UserStatsRepository userStatsRepository;
	private final AuthService authService;

	public UserService(UsersRepository usersRepository, UserStatsRepository userStatsRepository,
	                   AuthService authService) {
		this.usersRepository = usersRepository;
		this.userStatsRepository = userStatsRepository;
		this.authService = authService;
	}

	/**
	 * Get the current authenticated user
	 */
	public Users getCurrentUser() {
		Jwt jwt = SecurityUtils.getCurrentJwt();
		if (jwt == null) {
			throw new AccessDeniedException("User not authenticated");
		}
		return authService.getAuthenticatedUser(jwt);
	}

	/**
	 * Get current authenticated user as DTO
	 */
	public UserResponseDTO getCurrentUserDTO() {
		return mapToResponseDTO(getCurrentUser());
	}

	/**
	 * Update current authenticated user's profile
	 */
	@Transactional
	public UserResponseDTO updateCurrentUser(UserRequestDTO userRequestDTO) {
		if (userRequestDTO == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		Users currentUser = getCurrentUser();

		// Update user properties if provided
		if (userRequestDTO.name() != null && !userRequestDTO.name().trim().isEmpty()) {
			currentUser.setName(userRequestDTO.name());
		}

		if (userRequestDTO.email() != null && !userRequestDTO.email().trim().isEmpty()) {
			// Check if email is already taken by another user
			Optional<Users> existingUser = usersRepository.findByEmail(userRequestDTO.email());
			if (existingUser.isPresent() && !existingUser.get().getUserId().equals(currentUser.getUserId())) {
				throw new IllegalArgumentException("Email already registered");
			}
			currentUser.setEmail(userRequestDTO.email());
		}

		if (userRequestDTO.phoneNumber() != null && !userRequestDTO.phoneNumber().trim().isEmpty()) {
			currentUser.setPhoneNumber(userRequestDTO.phoneNumber());
		}

		Users updatedUser = usersRepository.save(currentUser);
		return mapToResponseDTO(updatedUser);
	}

	/**
	 * Delete current authenticated user account
	 */
	@Transactional
	public void deleteCurrentUser() {
		Users currentUser = getCurrentUser();
		usersRepository.delete(currentUser);
	}

	/**
	 * Get user by ID (admin only or own profile)
	 */
	public UserResponseDTO getUserById(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Users currentUser = getCurrentUser();

		// Users can only view their own profile unless they have admin role
		if (!currentUser.getUserId().equals(userId) && !hasAdminRole()) {
			throw new AccessDeniedException("Access denied: cannot view other user's profile");
		}

		return usersRepository.findById(userId)
		                      .map(this::mapToResponseDTO)
		                      .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
	}

	/**
	 * Update user by ID (admin only)
	 */
	@Transactional
	public UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (userRequestDTO == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		// Only admins can update other users
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}

		Users existingUser = usersRepository.findById(userId)
		                                    .orElseThrow(() -> new RuntimeException(
			                                    "User not found with id: " + userId));

		// Update user properties if provided
		if (userRequestDTO.name() != null && !userRequestDTO.name().trim().isEmpty()) {
			existingUser.setName(userRequestDTO.name());
		}

		if (userRequestDTO.email() != null && !userRequestDTO.email().trim().isEmpty()) {
			// Check if email is already taken by another user
			Optional<Users> emailUser = usersRepository.findByEmail(userRequestDTO.email());
			if (emailUser.isPresent() && !emailUser.get().getUserId().equals(userId)) {
				throw new IllegalArgumentException("Email already registered");
			}
			existingUser.setEmail(userRequestDTO.email());
		}

		if (userRequestDTO.phoneNumber() != null && !userRequestDTO.phoneNumber().trim().isEmpty()) {
			existingUser.setPhoneNumber(userRequestDTO.phoneNumber());
		}

		Users updatedUser = usersRepository.save(existingUser);
		return mapToResponseDTO(updatedUser);
	}

	/**
	 * Delete user by ID (admin only)
	 */
	@Transactional
	public void deleteUser(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		// Only admins can delete users
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}

		if (!usersRepository.existsById(userId)) {
			throw new RuntimeException("User not found with id: " + userId);
		}

		usersRepository.deleteById(userId);
	}

	/**
	 * Get all users (admin only)
	 */
	public List<UserResponseDTO> getAllUsers() {
		// Only admins can view all users
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}

		return usersRepository.findAll().stream()
		                      .map(this::mapToResponseDTO)
		                      .collect(Collectors.toList());
	}

	/**
	 * Get current user's statistics
	 */
	public UserStatsResponseDTO getCurrentUserStats() {
		Users currentUser = getCurrentUser();
		return getUserStats(currentUser.getUserId());
	}

	/**
	 * Get user statistics by ID (admin only or own stats)
	 */
	public UserStatsResponseDTO getUserStats(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Users currentUser = getCurrentUser();

		// Users can only view their own stats unless they have admin role
		if (!currentUser.getUserId().equals(userId) && !hasAdminRole()) {
			throw new AccessDeniedException("Access denied: cannot view other user's statistics");
		}

		return userStatsRepository.findById(userId)
		                          .map(this::mapToStatsResponseDTO)
		                          .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
	}

	/**
	 * Get all user statistics (admin only)
	 */
	public List<UserStatsResponseDTO> getAllUserStats() {
		// Only admins can view all user stats
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}

		return userStatsRepository.findAll()
		                          .stream()
		                          .map(this::mapToStatsResponseDTO)
		                          .collect(Collectors.toList());
	}

	/**
	 * Check if current user has admin role
	 */
	private boolean hasAdminRole() {
		Jwt jwt = SecurityUtils.getCurrentJwt();
		if (jwt == null) return false;

		// Check realm_access.roles for admin role
		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof java.util.Map) {
			Object roles = ((java.util.Map<?, ?>) realmAccess).get("roles");
			if (roles instanceof java.util.List) {
				return ((java.util.List<?>) roles).contains("admin");
			}
		}
		return false;
	}

	/**
	 * Find user by email (admin only)
	 */
	public Optional<Users> findByEmail(String email) {
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}
		return usersRepository.findByEmail(email);
	}

	/**
	 * Create user manually (admin only) - for special cases
	 * Note: Users are normally created automatically via OAuth2
	 */
	@Transactional
	public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
		// Only admins can create users manually
		if (!hasAdminRole()) {
			throw new AccessDeniedException("Access denied: admin role required");
		}

		if (userRequestDTO == null) {
			throw new IllegalArgumentException("User data cannot be null");
		}

		if (userRequestDTO.name() == null || userRequestDTO.name().trim().isEmpty()) {
			throw new IllegalArgumentException("User name cannot be empty");
		}

		if (userRequestDTO.email() == null || userRequestDTO.email().trim().isEmpty()) {
			throw new IllegalArgumentException("User email cannot be empty");
		}

		if (userRequestDTO.phoneNumber() == null || userRequestDTO.phoneNumber().trim().isEmpty()) {
			throw new IllegalArgumentException("User phone cannot be empty");
		}

		// Check if email already exists
		if (usersRepository.existsByEmail(userRequestDTO.email())) {
			throw new IllegalArgumentException("Email already registered");
		}

		Users user = new Users();
		user.setName(userRequestDTO.name());
		user.setEmail(userRequestDTO.email());
		user.setPhoneNumber(userRequestDTO.phoneNumber());

		Users savedUser = usersRepository.save(user);
		return mapToResponseDTO(savedUser);
	}

	private UserResponseDTO mapToResponseDTO(Users user) {
		return new UserResponseDTO(user.getUserId(),
		                           user.getName(),
		                           user.getEmail(),
		                           user.getPhoneNumber(),
		                           user.getCreatedAt(),
		                           user.getUpdatedAt());
	}

	private UserStatsResponseDTO mapToStatsResponseDTO(UserStats stats) {
		return new UserStatsResponseDTO(
			stats.getUserId(),
			stats.getUserName(),
			stats.getUserEmail(),
			stats.getAccountCreatedAt(),
			stats.getTotalTransactions(),
			stats.getTotalExpenses(),
			stats.getTotalIncome(),
			stats.getTotalExpenseAmount(),
			stats.getTotalIncomeAmount(),
			stats.getNetBalance(),
			stats.getTotalDaysLogged(),
			stats.getFirstTransactionDate(),
			stats.getLastTransactionDate()
		);
	}
}
