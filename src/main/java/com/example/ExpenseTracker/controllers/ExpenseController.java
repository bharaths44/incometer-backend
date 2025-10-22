package com.example.ExpenseTracker.controllers;

import com.example.ExpenseTracker.entities.DTOs.ExpenseRequestDTO;
import com.example.ExpenseTracker.entities.DTOs.ExpenseResponseDTO;
import com.example.ExpenseTracker.service.ExpenseService;
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
		ExpenseResponseDTO response = expenseService.createExpense(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses() {
		return ResponseEntity.ok(expenseService.getAllExpenses());
	}

	// Update
	@PutMapping("/{id}")
	public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable Long id,
															@Valid @RequestBody ExpenseRequestDTO dto) {
		ExpenseResponseDTO response = expenseService.updateExpense(id, dto);
		return ResponseEntity.ok(response);
	}

	// Delete
	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteExpense(@PathVariable Long userId, @PathVariable Long id) {

		expenseService.deleteExpense(id, userId);
		return ResponseEntity.ok("Expense deleted successfully");
	}
}

