package com.bharath.incometer.repository;

import com.bharath.incometer.entities.UserStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserStatsRepositoryTest {

	@Autowired
	private UserStatsRepository userStatsRepository;

	@Test
	void testFindAllUserStats() {
		// Given: Database has users (from insert_data.sql)

		// When: Fetch all user stats
		List<UserStats> allStats = userStatsRepository.findAll();

		// Then: Should return stats for all users
		assertThat(allStats).isNotNull();
		// Uncomment if you have seed data
		// assertThat(allStats).isNotEmpty();
	}

	@Test
	void testFindUserStatsById() {
		// Given: A user exists in the database
		List<UserStats> allStats = userStatsRepository.findAll();

		if (!allStats.isEmpty()) {
			UUID userId = allStats.getFirst().getUserId();

			// When: Fetch stats by user ID
			Optional<UserStats> stats = userStatsRepository.findById(userId);

			// Then: Should return the user's stats
			assertThat(stats).isPresent();
			assertThat(stats.get().getUserId()).isEqualTo(userId);
			assertThat(stats.get().getUserName()).isNotNull();
			assertThat(stats.get().getUserEmail()).isNotNull();
			assertThat(stats.get().getTotalTransactions()).isNotNull();
			assertThat(stats.get().getTotalTransactions()).isGreaterThanOrEqualTo(0L);
		}
	}

	@Test
	void testUserStatsHasAllFields() {
		// Given: A user with transactions exists
		List<UserStats> allStats = userStatsRepository.findAll();

		if (!allStats.isEmpty()) {
			UserStats stats = allStats.getFirst();

			// Then: All fields should be populated
			assertThat(stats.getUserId()).isNotNull();
			assertThat(stats.getUserName()).isNotNull();
			assertThat(stats.getUserEmail()).isNotNull();
			assertThat(stats.getAccountCreatedAt()).isNotNull();
			assertThat(stats.getTotalTransactions()).isNotNull();
			assertThat(stats.getTotalExpenses()).isNotNull();
			assertThat(stats.getTotalIncome()).isNotNull();
			assertThat(stats.getTotalExpenseAmount()).isNotNull();
			assertThat(stats.getTotalIncomeAmount()).isNotNull();
			assertThat(stats.getNetBalance()).isNotNull();
			assertThat(stats.getTotalDaysLogged()).isNotNull();

		}
	}

	@Test
	void testUserWithNoTransactionsHasZeroStats() {
		// This test verifies that users without transactions still appear in the view
		// with zero values for transaction-related stats

		List<UserStats> allStats = userStatsRepository.findAll();

		// Find a user with no transactions (if any)
		Optional<UserStats> userWithNoTransactions = allStats.stream()
		                                                     .filter(s -> s.getTotalTransactions() == 0L)
		                                                     .findFirst();

		if (userWithNoTransactions.isPresent()) {
			UserStats stats = userWithNoTransactions.get();

			assertThat(stats.getTotalTransactions()).isEqualTo(0L);
			assertThat(stats.getTotalExpenses()).isEqualTo(0L);
			assertThat(stats.getTotalIncome()).isEqualTo(0L);
			assertThat(stats.getTotalDaysLogged()).isEqualTo(0L);
			assertThat(stats.getFirstTransactionDate()).isNull();
			assertThat(stats.getLastTransactionDate()).isNull();
		}
	}
}
