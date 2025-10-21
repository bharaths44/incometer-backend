package com.example.ExpenseTracker.controllers;

import com.example.ExpenseTracker.entities.DTOs.CategoryRequestDTO;
import com.example.ExpenseTracker.entities.DTOs.CategoryResponseDTO;
import com.example.ExpenseTracker.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping
	public ResponseEntity<CategoryResponseDTO> createCategory(CategoryRequestDTO categoryRequestDTO) {
		CategoryResponseDTO response = categoryService.addCategory(categoryRequestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}
	@GetMapping
	public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,CategoryRequestDTO categoryRequestDTO) {
		CategoryResponseDTO response = categoryService.updateCategory(id,categoryRequestDTO);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{userId}/{id}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long userId, @PathVariable Long id) {
		categoryService.deleteCategory(id, userId);
		return ResponseEntity.ok("Category deleted successfully");
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable Long id) {
		CategoryResponseDTO response = categoryService.getCategoryById(id);
		return ResponseEntity.ok(response);

	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByUserId(@PathVariable Long userId) {
		return ResponseEntity.ok(categoryService.getCategoriesByUserId(userId));
	}
}
