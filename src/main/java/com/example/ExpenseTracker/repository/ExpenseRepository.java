package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
}
