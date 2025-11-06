package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserStatsResponseDTO;
import com.bharath.incometer.entities.UserStats;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.repository.UserStatsRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

	@Mock
	private UserStatsRepository userStatsRepository;

	@Mock
	private AuthService authService;

	@InjectMocks
	private UserService userService;

	private UserStats mockUserStats;
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

		mockUserStats = new UserStats(testUserId,
		                              "Test User",
		                              "test@example.com",
		                              LocalDateTime.now().minusDays(30),
		                              100L,
		                              // totalTransactions
		                              80L,
		                              // totalExpenses
		                              20L,
		                              // totalIncome
		                              new BigDecimal("5000.00"),
		                              // totalExpenseAmount
		                              new BigDecimal("8000.00"),
		                              // totalIncomeAmount
		                              new BigDecimal("3000.00"),
		                              // netBalance
		                              25L,
		                              // totalDaysLogged
		                              LocalDateTime.now().minusDays(25),
		                              // firstTransactionDate
		                              LocalDateTime.now()
		                              // lastTransactionDate
		);

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

	@Test
	void testGetUserStats_Success() {
		// Given
		when(userStatsRepository.findById(testUserId)).thenReturn(Optional.of(mockUserStats));

		// When
		UserStatsResponseDTO result = userService.getUserStats(testUserId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.userId()).isEqualTo(testUserId);
		assertThat(result.userName()).isEqualTo("Test User");
		assertThat(result.userEmail()).isEqualTo("test@example.com");
		assertThat(result.totalTransactions()).isEqualTo(100L);
		assertThat(result.totalExpenses()).isEqualTo(80L);
		assertThat(result.totalIncome()).isEqualTo(20L);
		assertThat(result.totalExpenseAmount()).isEqualTo(new BigDecimal("5000.00"));
		assertThat(result.totalIncomeAmount()).isEqualTo(new BigDecimal("8000.00"));
		assertThat(result.netBalance()).isEqualTo(new BigDecimal("3000.00"));
		assertThat(result.totalDaysLogged()).isEqualTo(25L);
	}

	@Test
	void testGetUserStats_UserNotFound() {
		// Given - trying to access another user's stats without admin role
		UUID nonExistentUserId = UUID.randomUUID();
		lenient().when(userStatsRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

		// When & Then - should get access denied because user can't view other user's stats
		assertThatThrownBy(() -> userService.getUserStats(nonExistentUserId)).isInstanceOf(AccessDeniedException.class)
		                                                                     .hasMessageContaining("Access denied");
	}

	@Test
	void testGetUserStats_NullUserId() {
		// When & Then
		assertThatThrownBy(() -> userService.getUserStats(null)).isInstanceOf(IllegalArgumentException.class)
		                                                        .hasMessageContaining("User ID cannot be null");
	}

	@Test
	void testGetAllUserStats_Success() {
		// Given - non-admin user should get access denied
		UserStats secondUserStats = new UserStats(UUID.randomUUID(),
		                                          "Second User",
		                                          "second@example.com",
		                                          LocalDateTime.now().minusDays(60),
		                                          50L,
		                                          40L,
		                                          10L,
		                                          new BigDecimal("2000.00"),
		                                          new BigDecimal("4000.00"),
		                                          new BigDecimal("2000.00"),
		                                          15L,
		                                          LocalDateTime.now().minusDays(50),
		                                          LocalDateTime.now());

		List<UserStats> mockStatsList = Arrays.asList(mockUserStats, secondUserStats);
		lenient().when(userStatsRepository.findAll()).thenReturn(mockStatsList);

		// When & Then - should get access denied because only admins can view all stats
		assertThatThrownBy(() -> userService.getAllUserStats()).isInstanceOf(AccessDeniedException.class)
		                                                       .hasMessageContaining(
			                                                       "Access denied: admin role required");
	}

	@Test
	void testGetAllUserStats_EmptyList() {
		// Given - non-admin user should get access denied
		lenient().when(userStatsRepository.findAll()).thenReturn(List.of());

		// When & Then - should get access denied because only admins can view all stats
		assertThatThrownBy(() -> userService.getAllUserStats()).isInstanceOf(AccessDeniedException.class)
		                                                       .hasMessageContaining(
			                                                       "Access denied: admin role required");
	}
}
