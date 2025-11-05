package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	Users findByPhoneNumber(String from);
}
