package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserUserId(UUID userId);

	boolean existsByUserUserIdAndNameAndType(UUID user_userId, String name, TransactionType type);

	Category findByUserUserIdAndNameIgnoreCase(UUID userId, String name);

	List<Category> findByUserUserIdAndType(UUID userId, TransactionType type);
}
