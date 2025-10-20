package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
