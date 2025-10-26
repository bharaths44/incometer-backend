package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
	boolean existsByEmail(String email);

	Users findByPhoneNumber(String phoneNumber);

	boolean existsByPhoneNumber(String from);
}
