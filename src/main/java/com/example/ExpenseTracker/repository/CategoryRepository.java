package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entities.Category;
import com.example.ExpenseTracker.entities.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserUserId(Long userId);

	boolean existsByUserUserIdAndNameAndType(Long aLong, String name, TransactionType type);

	Optional<Category> findByUserUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);


	Category findByUserUserIdAndNameIgnoreCase(Long userId, String name);


	Category findByUserUserIdAndName(Long userId, String categoryName);
}
