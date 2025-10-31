package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

//	List<Budget> findByUserUserId(Long userId);

	@Query("SELECT b FROM Budget b WHERE b.user.userId = :userId AND b.active = true AND :currentDate BETWEEN b" +
	       ".startDate AND b.endDate")
	List<Budget> findActiveBudgetsForUserAndDate(
		@Param("userId") Long userId,
		@Param("currentDate") LocalDate currentDate);

//	@Query(
//		"SELECT b FROM Budget b WHERE b.user.userId = :userId AND b.category.categoryId = :categoryId AND b.active " +
//		"= true AND :currentDate BETWEEN b.startDate AND b.endDate")
//	List<Budget> findActiveBudgetsForUserCategoryAndDate(
//		@Param("userId") Long userId,
//		@Param("categoryId") Long categoryId,
//		@Param("currentDate") LocalDate currentDate);

	@Modifying
	@Query("DELETE FROM Budget b WHERE b.category.categoryId = :categoryId")
	void deleteByCategoryId(
		@Param("categoryId") Long categoryId);
}
