package com.example.expensetracker.repository;
import com.example.expensetracker.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsersRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);

	Users findByPhoneNumber(String phoneNumber);

	boolean existsByPhoneNumber(String from);
}
