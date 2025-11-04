package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
	boolean existsByEmail(String email);

	Users findByPhoneNumber(String phoneNumber);

	boolean existsByPhoneNumber(String from);

}
