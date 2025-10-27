package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserUserId(Long userId);

	boolean existsByUserUserIdAndNameAndType(Long aLong, String name, TransactionType type);

	Category findByUserUserIdAndNameIgnoreCase(Long userId, String name);

	List<Category> findByUserUserIdAndType(Long userId, TransactionType type);
}
