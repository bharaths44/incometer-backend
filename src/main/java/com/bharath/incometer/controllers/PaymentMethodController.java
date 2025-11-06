package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.PaymentMethodRequestDTO;
import com.bharath.incometer.entities.DTOs.PaymentMethodResponseDTO;
import com.bharath.incometer.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

	private final PaymentMethodService service;

	@GetMapping
	public ResponseEntity<List<PaymentMethodResponseDTO>> getAll() {
		List<PaymentMethodResponseDTO> dto = service.getAll();
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PaymentMethodResponseDTO> getById(
		@PathVariable Long id) {
		return service.getById(id)
		              .map(ResponseEntity::ok)
		              .orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<PaymentMethodResponseDTO>> getByUser(
		@PathVariable Long userId) {
		List<PaymentMethodResponseDTO> dtos = service.getByUserId(userId);
		return ResponseEntity.ok(dtos);
	}

	@PostMapping
	public ResponseEntity<PaymentMethodResponseDTO> create(
		@RequestBody PaymentMethodRequestDTO dto,
		@RequestParam(required = false) Long userId) {
		PaymentMethodResponseDTO response = service.create(dto, userId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PaymentMethodResponseDTO> update(
		@PathVariable Long id,
		@RequestBody PaymentMethodRequestDTO dto,
		@RequestParam(required = false) Long userId) {
		try {
			PaymentMethodResponseDTO response = service.update(id, dto, userId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
		@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
