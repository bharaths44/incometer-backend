package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.UserStatsResponseDTO;
import com.bharath.incometer.entities.UserStats;
import com.bharath.incometer.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

	@Mock
	private UserStatsRepository userStatsRepository;

	@InjectMocks
	private UserService userService;

	private UserStats mockUserStats;
	private UUID testUserId;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();
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
		// Given
		UUID nonExistentUserId = UUID.randomUUID();
		when(userStatsRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> userService.getUserStats(nonExistentUserId)).isInstanceOf(RuntimeException.class)
		                                                                     .hasMessageContaining("User not found");
	}

	@Test
	void testGetUserStats_NullUserId() {
		// When & Then
		assertThatThrownBy(() -> userService.getUserStats(null)).isInstanceOf(IllegalArgumentException.class)
		                                                        .hasMessageContaining("User ID cannot be null");
	}

	@Test
	void testGetAllUserStats_Success() {
		// Given
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
		when(userStatsRepository.findAll()).thenReturn(mockStatsList);

		// When
		List<UserStatsResponseDTO> result = userService.getAllUserStats();

		// Then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).userName()).isEqualTo("Test User");
		assertThat(result.get(1).userName()).isEqualTo("Second User");
	}

	@Test
	void testGetAllUserStats_EmptyList() {
		// Given
		when(userStatsRepository.findAll()).thenReturn(List.of());

		// When
		List<UserStatsResponseDTO> result = userService.getAllUserStats();

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}
}
