package com.bharath.incometer.controllers;

import com.bharath.incometer.entities.DTOs.BudgetRequestDTO;
import com.bharath.incometer.entities.DTOs.BudgetResponseDTO;
import com.bharath.incometer.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

	private final BudgetService budgetService;

	public BudgetController(BudgetService budgetService) {
		this.budgetService = budgetService;
	}

	@PostMapping
	public ResponseEntity<BudgetResponseDTO> createBudget(
		@RequestBody BudgetRequestDTO budgetRequestDTO) {
		System.out.println("Received Budget Request: " + budgetRequestDTO);
		try {
			BudgetResponseDTO response = budgetService.createBudget(budgetRequestDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error creating budget: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<BudgetResponseDTO> getBudgetById(
		@PathVariable Long id) {
		try {
			BudgetResponseDTO budget = budgetService.getBudgetById(id);
			return ResponseEntity.ok(budget);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving budget: " + e.getMessage());
		}
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByUser(
		@PathVariable Long userId) {
		try {
			List<BudgetResponseDTO> budgets = budgetService.getBudgetsByUser(userId);
			return ResponseEntity.ok(budgets);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving budgets for user: " + e.getMessage());
		}
	}

	@GetMapping("/user/{userId}/date")
	public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByUserAndDate(
		@PathVariable Long userId,
		@RequestParam(required = false) LocalDate date) {
		try {
			LocalDate queryDate = date != null ? date : LocalDate.now();
			List<BudgetResponseDTO> budgets = budgetService.getBudgetsByUserAndDate(userId, queryDate);
			return ResponseEntity.ok(budgets);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving budgets for user and date: " + e.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<BudgetResponseDTO> updateBudget(
		@PathVariable Long id,
		@RequestBody BudgetRequestDTO budgetRequestDTO) {
		System.out.println("Received Budget Update Request for ID: " + id);
		try {
			BudgetResponseDTO response = budgetService.updateBudget(id, budgetRequestDTO);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error updating budget: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBudget(
		@PathVariable Long id,
		@RequestParam Long userId) {
		try {
			budgetService.deleteBudget(id, userId);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error deleting budget: " + e.getMessage());
		}
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<BudgetResponseDTO> deactivateBudget(
		@PathVariable Long id,
		@RequestParam Long userId) {
		System.out.println("Deactivating Budget with ID: " + id);
		try {
			BudgetResponseDTO response = budgetService.deactivateBudget(id, userId);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error deactivating budget: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets() {
		try {
			return ResponseEntity.ok(budgetService.getAllBudgets());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving budgets: " + e.getMessage());
		}
	}
}

