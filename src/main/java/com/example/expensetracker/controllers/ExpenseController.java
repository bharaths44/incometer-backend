package com.example.expensetracker.controllers;

import com.example.expensetracker.entities.DTOs.ExpenseRequestDTO;
import com.example.expensetracker.entities.DTOs.ExpenseResponseDTO;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/expenses")
public class ExpenseController {
	private final ExpenseService expenseService;

	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello, Expense Tracker!";
	}


	@PostMapping
	public ResponseEntity<ExpenseResponseDTO> addExpense(@Valid @RequestBody ExpenseRequestDTO dto) {
		try {
			ExpenseResponseDTO response = expenseService.createExpense(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user/category not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error creating expense: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses() {
		try {
			return ResponseEntity.ok(expenseService.getAllExpenses());
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for resource not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving expenses: " + e.getMessage());
		}
	}

	// Update
	@PutMapping("/{id}")
	public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable Long id,
															@Valid @RequestBody ExpenseRequestDTO dto) {
		try {
			ExpenseResponseDTO response = expenseService.updateExpense(id, dto);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for expense not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error updating expense: " + e.getMessage());
		}
	}

	// Delete
	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteExpense(@PathVariable Long userId, @PathVariable Long id) {
		try {
			expenseService.deleteExpense(id, userId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Expense has been deleted");
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for expense not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error deleting expense: " + e.getMessage());
		}
	}
}

