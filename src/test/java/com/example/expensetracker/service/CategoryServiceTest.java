package com.example.expensetracker.service;

import com.example.expensetracker.entities.Category;
import com.example.expensetracker.entities.DTOs.CategoryRequestDTO;
import com.example.expensetracker.entities.DTOs.CategoryResponseDTO;
import com.example.expensetracker.entities.TransactionType;
import com.example.expensetracker.entities.Users;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private UsersRepository usersRepository;

	@InjectMocks
	private CategoryService categoryService;

	private Users user;
	private Category category;

	@BeforeEach
	void setUp() {
		user = new Users();
		user.setUserId(1L);
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("1234567890");
		user.setPassword("password");

		category = new Category();
		category.setCategoryId(1L);
		category.setUser(user);
		category.setName("Food");
		category.setType(TransactionType.EXPENSE);
		category.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void testAddCategory() {
		CategoryRequestDTO request = new CategoryRequestDTO(1L, "Food", TransactionType.EXPENSE);
		when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
		when(categoryRepository.existsByUserUserIdAndNameAndType(1L,
																 "Food",
																 TransactionType.EXPENSE)).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		System.out.println("Input: " + request);
		CategoryResponseDTO expected = new CategoryResponseDTO(1L,
															   1L,
															   "Food",
															   TransactionType.EXPENSE,
															   category.getCreatedAt());
		System.out.println("Expected: " + expected);

		CategoryResponseDTO result = categoryService.addCategory(request);
		System.out.println("Real Output: " + result);

		assertEquals(expected.categoryId(), result.categoryId());
		assertEquals(expected.userId(), result.userId());
		assertEquals(expected.name(), result.name());
		assertEquals(expected.type(), result.type());
	}

	@Test
	void testGetCategoriesByUserId() {
		when(categoryRepository.findByUserUserId(1L)).thenReturn(Collections.singletonList(category));

		System.out.println("Input: userId=1L");
		List<CategoryResponseDTO> expected = List.of(new CategoryResponseDTO(1L,
																			 1L,
																			 "Food",
																			 TransactionType.EXPENSE,
																			 category.getCreatedAt()));
		System.out.println("Expected: " + expected);

		List<CategoryResponseDTO> result = categoryService.getCategoriesByUserId(1L);
		System.out.println("Real Output: " + result);

		assertEquals(1, result.size());
		assertEquals("Food", result.getFirst().name());
	}

	@Test
	void testGetCategoryIdByName() {
		when(categoryRepository.findByUserUserIdAndNameIgnoreCase(1L, "food")).thenReturn(category);

		System.out.println("Input: categoryName='food', userId=1L");
		Long expected = 1L;
		System.out.println("Expected: " + expected);

		Long result = categoryService.getCategoryIdByName("food", 1L);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testCreateCategoryForUser() {
		when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		System.out.println("Input: categoryName='food', userId=1L, type=EXPENSE");
		Long expected = 1L;
		System.out.println("Expected: " + expected);

		Long result = categoryService.createCategoryForUser("food", 1L, TransactionType.EXPENSE);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
