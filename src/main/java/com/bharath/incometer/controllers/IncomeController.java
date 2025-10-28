package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.IncomeRequestDTO;
import com.bharath.incometer.entities.DTOs.IncomeResponseDTO;
import com.bharath.incometer.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {
	private final IncomeService incomeService;

	public IncomeController(IncomeService incomeService) {
		this.incomeService = incomeService;
	}

	@PostMapping
	public ResponseEntity<IncomeResponseDTO> addIncome(
		@Valid
		@RequestBody
		IncomeRequestDTO dto) {
		try {
			IncomeResponseDTO response = incomeService.createIncome(dto);
			return ResponseEntity.status(HttpStatus.CREATED)
			                     .body(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error creating income: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<IncomeResponseDTO>> getAllIncomes() {
		try {
			return ResponseEntity.ok(incomeService.getAllIncomes());
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for resource not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving incomes: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<IncomeResponseDTO> getIncome(
		@PathVariable Long id) {
		try {

			IncomeResponseDTO response = incomeService.getIncomeById(id, null); // Adjust as needed
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for income not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving income: " + e.getMessage());
		}
	}

	// Update
	@PutMapping("/{id}")
	public ResponseEntity<IncomeResponseDTO> updateIncome(
		@PathVariable Long id,
		@Valid
		@RequestBody
		IncomeRequestDTO dto) {
		try {
			IncomeResponseDTO response = incomeService.updateIncome(id, dto);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for income not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error updating income: " + e.getMessage());
		}
	}

	// Delete
	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteIncome(
		@PathVariable Long userId,
		@PathVariable Long id) {
		try {
			incomeService.deleteIncome(id, userId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
			                     .body("Income has been deleted");
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for income not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error deleting income: " + e.getMessage());
		}
	}
}
