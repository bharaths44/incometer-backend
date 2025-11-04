package com.bharath.incometer.repository;

import com.bharath.incometer.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
	List<PaymentMethod> findByUserUserId(UUID userId);
}
