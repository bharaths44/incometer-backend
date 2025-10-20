package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
