package com.example.expensetracker.service;

import com.example.expensetracker.entities.Category;
import com.example.expensetracker.entities.DTOs.CategoryRequestDTO;
import com.example.expensetracker.entities.DTOs.CategoryResponseDTO;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

	private final ExpenseRepository expenseRepository;
	private final CategoryRepository categoryRepository;
	private final UsersRepository usersRepository;

	public CategoryService(ExpenseRepository expenseRepository,
						   CategoryRepository categoryRepository,
						   UsersRepository usersRepository) {
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		this.usersRepository = usersRepository;
	}

	private CategoryResponseDTO toDTO(Category category) {
		return new CategoryResponseDTO(
				category.getCategoryId(),
				category.getUser().getUserId(),
				category.getName(),
				category.getType(),
				category.getCreatedAt()
		);
	}

	private Category toEntity(CategoryRequestDTO categoryRequestDTO) {
		Category category = new Category();
		category.setUser(usersRepository.findById(categoryRequestDTO.userId())
										.orElseThrow(() -> new RuntimeException("User not found with id: " + categoryRequestDTO.userId())));
		category.setName(categoryRequestDTO.name());
		category.setType(categoryRequestDTO.type());
		return category;
	}

	@Transactional
	public CategoryResponseDTO addCategory(CategoryRequestDTO dto) {
		// Validate input
		if (dto == null || dto.name() == null || dto.name().trim().isEmpty()) {
			throw new IllegalArgumentException("Category name cannot be null or empty");
		}

		// Check if category already exists for this user
		boolean exists = categoryRepository.existsByUserUserIdAndNameAndType(
				dto.userId(), dto.name(), dto.type());
		if (exists) {
			throw new RuntimeException("Category already exists for this user");
		}

		Category category = toEntity(dto);
		Category savedCategory = categoryRepository.save(category);
		return toDTO(savedCategory);
	}

	@Transactional
	public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
		if (id == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}

		Category existingCategory = categoryRepository.findById(id)
													  .orElseThrow(() -> new RuntimeException(
															  "Category not found with id: " + id));

		// Validate that user owns this category
		if (!existingCategory.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("User does not have permission to update this category");
		}

		existingCategory.setName(dto.name());
		existingCategory.setType(dto.type());
		Category updatedCategory = categoryRepository.save(existingCategory);
		return toDTO(updatedCategory);
	}

	@Transactional
	public void deleteCategory(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Category existingCategory = categoryRepository.findById(id)
													  .orElseThrow(() -> new RuntimeException(
															  "Category not found with id: " + id));

		// Validate that user owns this category
		if (!existingCategory.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to delete this category");
		}

		// Check if category has associated expenses
		boolean hasExpenses = expenseRepository.existsByCategoryCategoryId(id);
		if (hasExpenses) {
			throw new RuntimeException("Cannot delete category with associated expenses");
		}

		categoryRepository.delete(existingCategory);
	}

	@Transactional(readOnly = true)
	public CategoryResponseDTO getCategoryById(Long id) {
		Category category = categoryRepository.findById(id)
											  .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
		return toDTO(category);
	}

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> getAllCategories() {
		return categoryRepository.findAll()
								 .stream()
								 .map(this::toDTO)
								 .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> getCategoriesByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return categoryRepository.findByUserUserId(userId)
								 .stream()
								 .map(this::toDTO)
								 .collect(Collectors.toList());
	}

	public Long getCategoryIdByName(String categoryName, Long userId) {
		Category category = categoryRepository.findByUserUserIdAndNameIgnoreCase(userId, categoryName);
		return category != null ? category.getCategoryId() : null;
	}
}