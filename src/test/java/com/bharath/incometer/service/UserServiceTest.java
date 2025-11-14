package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserRequestDTO;
import com.bharath.incometer.entities.DTOs.UserResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private AuthService authService;

	@InjectMocks
	private UserService userService;

	private Users user;
	private Jwt mockJwt;

	@BeforeEach
	void setUp() {
		user = new Users();
		user.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("1234567890");
		user.setRole(Role.USER);
		user.setProvider(AuthProvider.local);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		// Create mock JWT
		mockJwt = Jwt.withTokenValue("mock-token")
		             .header("alg", "RS256")
		             .claim("sub", user.getEmail())
		             .build();

		// Set up security context with JWT
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		Authentication authentication = new Authentication() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return Collections.emptyList();
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return mockJwt;
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			}

			@Override
			public String getName() {
				return user.getUserId().toString();
			}
		};
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Mock auth service - only needed for tests that call getCurrentUser
		lenient().when(authService.getAuthenticatedUser(mockJwt)).thenReturn(user);

		// Mock repository for getCurrentUser
		lenient().when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
	}

	@Test
	void testCreateUser() {
		UserRequestDTO request = new UserRequestDTO("Test User", "test@example.com", "1234567890", "password");

		// Non-admin user should get access denied
		assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(AccessDeniedException.class)
		                                                         .hasMessage("Access denied: admin role required");
	}

	@Test
	void testGetUserById() {
		when(usersRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(
			user));
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
		// Non-admin user should get access denied
		assertThatThrownBy(() -> userService.getAllUsers()).isInstanceOf(AccessDeniedException.class)
		                                                   .hasMessage("Access denied: admin role required");
	}
}
