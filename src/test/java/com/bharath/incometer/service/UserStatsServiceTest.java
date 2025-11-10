package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.repository.UserStatsRepository;
import com.bharath.incometer.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

	@Mock
	private UserStatsRepository userStatsRepository;

	@Mock
	private AuthService authService;

	private UUID testUserId;

	private Jwt mockJwt;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();

		// Create test user
		Users testUser = new Users();
		testUser.setUserId(testUserId);
		testUser.setName("Test User");
		testUser.setEmail("test@example.com");
		testUser.setRole(Role.USER);
		testUser.setProvider(AuthProvider.local);
		testUser.setCreatedAt(LocalDateTime.now());
		testUser.setUpdatedAt(LocalDateTime.now());

		// Create mock JWT
		mockJwt = Jwt.withTokenValue("mock-token").header("alg", "RS256").claim("sub", testUserId.toString()).build();

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
				return testUserId.toString();
			}
		};
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Mock auth service - lenient because not all tests need authentication
		lenient().when(authService.getAuthenticatedUser(mockJwt)).thenReturn(testUser);
	}

}