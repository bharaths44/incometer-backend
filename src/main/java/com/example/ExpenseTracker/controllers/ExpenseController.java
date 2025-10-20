package com.example.ExpenseTracker.controllers;

import com.example.ExpenseTracker.entities.DTOs.ExpenseRequestDTO;
import com.example.ExpenseTracker.entities.DTOs.ExpenseResponseDTO;
import com.example.ExpenseTracker.entities.Expense;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/expenses")
public class ExpenseController {

	private final ExpenseRepository expenseRepository;
	private final ExpenseService expenseService;


	public ExpenseController(ExpenseRepository expenseRepository, ExpenseService expenseService) {
		this.expenseRepository = expenseRepository;
		this.expenseService = expenseService;
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello, Expense Tracker!";
	}

	@GetMapping("/{id}")
	public Expense getExpenseById(@PathVariable Long id) {
		return expenseRepository.findById(id)
								.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
																			   "Expense not found"));
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
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteExpense(@PathVariable Long id) {
		expenseService.deleteExpense(id);
		return ResponseEntity.ok("Expense deleted successfully");
	}
}

