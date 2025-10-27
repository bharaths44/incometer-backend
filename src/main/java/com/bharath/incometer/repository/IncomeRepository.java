package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

	List<Income> findByUserUserId(Long userId);

	@Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.userId = :userId")
	BigDecimal sumAmountByUserId(@Param("userId") Long userId);

	@Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.userId = :userId AND i.receivedDate BETWEEN :startDate AND" +
	       " :endDate")
	BigDecimal sumAmountByUserIdAndDateRange(@Param("userId") Long userId,
	                                         @Param("startDate") LocalDate startDate,
	                                         @Param("endDate") LocalDate endDate);
}
