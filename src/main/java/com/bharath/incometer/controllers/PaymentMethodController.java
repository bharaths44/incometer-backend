package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.PaymentMethodRequestDTO;
import com.bharath.incometer.entities.DTOs.PaymentMethodResponseDTO;
import com.bharath.incometer.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

	private final PaymentMethodService paymentMethodService;

	@GetMapping
	public ResponseEntity<List<PaymentMethodResponseDTO>> getAll() {
		List<PaymentMethodResponseDTO> dto = paymentMethodService.getAll();
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PaymentMethodResponseDTO> getById(
		@PathVariable Long id) {
		return paymentMethodService.getById(id)
		                           .map(ResponseEntity::ok)
		                           .orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<PaymentMethodResponseDTO>> getByUser(
		@PathVariable UUID userId) {
		List<PaymentMethodResponseDTO> dtos = paymentMethodService.getByUserId(userId);
		return ResponseEntity.ok(dtos);
	}

	@PostMapping
	public ResponseEntity<PaymentMethodResponseDTO> create(
		@RequestBody PaymentMethodRequestDTO dto,
		@RequestParam(required = false) UUID userId) {
		PaymentMethodResponseDTO response = paymentMethodService.create(dto, userId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PaymentMethodResponseDTO> update(
		@PathVariable Long id,
		@RequestBody PaymentMethodRequestDTO dto,
		@RequestParam(required = false) UUID userId) {
		try {
			PaymentMethodResponseDTO response = paymentMethodService.update(id, dto, userId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@PathVariable Long id) {
		paymentMethodService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
