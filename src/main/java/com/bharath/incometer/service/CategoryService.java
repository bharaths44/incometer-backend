package com.bharath.incometer.service;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.CategoryRequestDTO;
import com.bharath.incometer.entities.DTOs.CategoryResponseDTO;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.BudgetRepository;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.TransactionRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

	private final TransactionRepository transactionRepository;
	private final CategoryRepository categoryRepository;
	private final UsersRepository usersRepository;
	private final BudgetRepository budgetRepository;

	public CategoryService(TransactionRepository transactionRepository,
	                       CategoryRepository categoryRepository,
	                       UsersRepository usersRepository,
	                       BudgetRepository budgetRepository) {
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.usersRepository = usersRepository;
		this.budgetRepository = budgetRepository;
	}

	private CategoryResponseDTO toDTO(Category category) {
		return new CategoryResponseDTO(category.getCategoryId(),
		                               category.getUser().getUserId(),
		                               category.getName(),
		                               category.getIcon(),
		                               category.getType(),
		                               category.getCreatedAt());
	}

	private Category toEntity(CategoryRequestDTO categoryRequestDTO) {
		Category category = new Category();
		category.setUser(usersRepository.findById(categoryRequestDTO.userId()).orElseThrow(()
			                                                                                   -> new RuntimeException(
			"User not found with id: " + categoryRequestDTO.userId())));
		category.setName(categoryRequestDTO.name());
		category.setIcon(categoryRequestDTO.icon());
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
		boolean exists = categoryRepository.existsByUserUserIdAndNameAndType(dto.userId(), dto.name(), dto.type());
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
		existingCategory.setIcon(dto.icon());
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

		budgetRepository.deleteByCategoryCategoryId(id);

		transactionRepository.deleteByCategoryCategoryId(id);

		// Delete the category
		categoryRepository.delete(existingCategory);
	}

	@Transactional(readOnly = true)
	public CategoryResponseDTO getCategoryById(Long id) {
		Category category = categoryRepository.findById(id)
		                                      .orElseThrow(() -> new RuntimeException(
			                                      "Category not found with id: " + id));
		return toDTO(category);
	}

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> getAllCategories() {
		return categoryRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> getCategoriesByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return categoryRepository.findByUserUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
	}

	public Long getCategoryIdByName(String categoryName, Long userId) {
		Category category = categoryRepository.findByUserUserIdAndNameIgnoreCase(userId, categoryName);
		return category != null ? category.getCategoryId() : null;
	}

	public List<String> getAllCategoryNamesForUserByType(Long userId, TransactionType type) {
		// Get user categories by type
		List<Category> userCategories = categoryRepository.findByUserUserIdAndType(userId, type);
		return userCategories.stream().map(Category::getName).collect(Collectors.toList());
	}

	@Transactional
	public Long createCategoryForUser(String categoryName, Long userId, TransactionType type) {
		Category category = new Category();
		category.setName(capitalize(categoryName));
		category.setIcon("circle"); // Default icon
		category.setType(type);
		// Set user
		category.setUser(usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
		Category saved = categoryRepository.save(category);
		return saved.getCategoryId();
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
}