package com.example.expensetracker.controllers;

import com.example.expensetracker.entities.DTOs.CategoryRequestDTO;
import com.example.expensetracker.entities.DTOs.CategoryResponseDTO;
import com.example.expensetracker.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping
	public ResponseEntity<CategoryResponseDTO> createCategory(CategoryRequestDTO categoryRequestDTO) {
		try {
			CategoryResponseDTO response = categoryService.addCategory(categoryRequestDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error creating category: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
		try {
			return ResponseEntity.ok(categoryService.getAllCategories());
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for resource not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving categories: " + e.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
															  CategoryRequestDTO categoryRequestDTO) {
		try {
			CategoryResponseDTO response = categoryService.updateCategory(id, categoryRequestDTO);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for category not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error updating category: " + e.getMessage());
		}
	}

	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long userId, @PathVariable Long id) {
		try {
			categoryService.deleteCategory(id, userId);
			return ResponseEntity.ok("Category deleted successfully");
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for category not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error deleting category: " + e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable Long id) {
		try {
			CategoryResponseDTO response = categoryService.getCategoryById(id);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for category not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving category: " + e.getMessage());
		}
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByUserId(@PathVariable Long userId) {
		try {
			return ResponseEntity.ok(categoryService.getCategoriesByUserId(userId));
		} catch (IllegalArgumentException e) {
			// Return 400 Bad Request for validation errors
			throw e;
		} catch (RuntimeException e) {
			// Return 404 Not Found for user not found
			throw e;
		} catch (Exception e) {
			// Return 500 for unexpected errors
			throw new RuntimeException("Error retrieving categories for user: " + e.getMessage());
		}
	}
}
