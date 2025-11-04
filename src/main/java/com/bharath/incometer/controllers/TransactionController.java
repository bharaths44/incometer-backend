package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.DTOs.TransactionResponseDTO;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<TransactionResponseDTO> addTransaction(
		@Valid
		@RequestBody
		TransactionRequestDTO dto) {
		try {
			TransactionResponseDTO response = transactionService.createTransaction(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user/category not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error creating transaction: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions(
		@RequestParam UUID userId,
		@RequestParam(required = false) TransactionType type) {
		try {
			List<TransactionResponseDTO> response;
			if (type != null) {
				response = transactionService.getTransactionsByUserIdAndType(userId, type);
			} else {
				response = transactionService.getTransactionsByUserId(userId);
			}
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for resource not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving transactions: " + e.getMessage());
		}
	}

	// Update
	@PutMapping("/{id}")
	public ResponseEntity<TransactionResponseDTO> updateTransaction(
		@PathVariable Long id,
		@Valid
		@RequestBody
		TransactionRequestDTO dto) {
		try {
			TransactionResponseDTO response = transactionService.updateTransaction(id, dto);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for expense not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error updating transaction: " + e.getMessage());
		}
	}

	// Delete
	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteTransaction(
		@PathVariable UUID userId,
		@PathVariable Long id) {
		try {
			transactionService.deleteTransaction(id, userId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Transaction has been deleted");
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for expense not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error deleting transaction: " + e.getMessage());
		}
	}

	@GetMapping("/{userId}/{id}")
	public ResponseEntity<TransactionResponseDTO> getTransactionById(
		@PathVariable UUID userId,
		@PathVariable Long id) {
		try {
			TransactionResponseDTO response = transactionService.getTransactionById(id, userId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for expense not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving transaction: " + e.getMessage());
		}
	}

	@GetMapping("/{userId}/date-range")
	public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
		@PathVariable UUID userId,
		@RequestParam LocalDate startDate,
		@RequestParam LocalDate endDate,
		@RequestParam(required = false) TransactionType type) {
		try {
			List<TransactionResponseDTO> response;
			if (type != null) {
				response = transactionService.getTransactionsByUserIdAndTypeAndDateRange(userId,
				                                                                         type,
				                                                                         startDate,
				                                                                         endDate);
			} else {
				response = transactionService.getTransactionsByUserIdAndDateRange(userId, startDate, endDate);
			}
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for resource not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving transactions: " + e.getMessage());
		}
	}
}