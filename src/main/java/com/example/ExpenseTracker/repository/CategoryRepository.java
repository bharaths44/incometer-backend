package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entities.Category;
import com.example.ExpenseTracker.entities.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserUserId(Long userId);

	boolean existsByUserUserIdAndNameAndType(Long aLong, String name, TransactionType type);
}
