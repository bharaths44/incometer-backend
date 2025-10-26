package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
	boolean existsByCategoryCategoryId(Long categoryId);

	List<Expense> findByUserUserId(Long userId);

	List<Expense> findByUserUserIdAndExpenseDateBetween(
			Long userId, LocalDate startDate, LocalDate endDate);

	List<Expense> findByUserUserIdAndCategoryCategoryId(Long userId, Long categoryId);

	@Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.userId = :userId")
	BigDecimal sumAmountByUserId(@Param("userId") Long userId);

	@Query(
			"SELECT SUM(e.amount) FROM Expense e WHERE e.user.userId = :userId " +
			"AND e.expenseDate BETWEEN :startDate AND :endDate"
	)
	BigDecimal sumAmountByUserIdAndDateRange(
			@Param("userId") Long userId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);
}
