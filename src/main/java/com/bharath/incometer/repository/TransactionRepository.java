package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
//	boolean existsByCategoryCategoryId(Long categoryId);

	List<Transaction> findByUserUserId(Long userId);

	List<Transaction> findByUserUserIdAndTransactionDateBetween(
		Long userId, LocalDate startDate, LocalDate endDate);

//	List<Transaction> findByUserUserIdAndCategoryCategoryId(Long userId, Long categoryId);

	List<Transaction> findByUserUserIdAndTransactionType(Long userId, TransactionType transactionType);

	List<Transaction> findByUserUserIdAndTransactionTypeAndTransactionDateBetween(
		Long userId, TransactionType transactionType, LocalDate startDate, LocalDate endDate);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId")
	BigDecimal sumAmountByUserId(
		@Param("userId") Long userId);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId AND t.transactionType = " +
	       ":transactionType")
	BigDecimal sumAmountByUserIdAndType(
		@Param("userId") Long userId,
		@Param("transactionType") TransactionType transactionType);

//	@Query(
//		"SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId " +
//		"AND t.transactionDate BETWEEN :startDate AND :endDate"
//	)

	/// /	BigDecimal sumAmountByUserIdAndDateRange(
//		@Param("userId") Long userId,
//		@Param("startDate") LocalDate startDate,
//		@Param("endDate") LocalDate endDate);
	@Query(
		"SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId AND t.transactionType = " +
		":transactionType " +
		"AND t.transactionDate BETWEEN :startDate AND :endDate"
	)
	BigDecimal sumAmountByUserIdAndTypeAndDateRange(
		@Param("userId") Long userId,
		@Param("transactionType") TransactionType transactionType,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);

	@Query(
		"SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId " +
		"AND t.category.categoryId = :categoryId " +
		"AND t.transactionDate BETWEEN :startDate AND :endDate"
	)
	BigDecimal sumAmountByUserIdAndCategoryIdAndDateRange(
		@Param("userId") Long userId,
		@Param("categoryId") Long categoryId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);

	@Modifying
	@Query("DELETE FROM Transaction t WHERE t.category.categoryId = :categoryId")
	void deleteByCategoryId(
		@Param("categoryId") Long categoryId);

	void deleteByPaymentMethodPaymentMethodId(Long id);
}
