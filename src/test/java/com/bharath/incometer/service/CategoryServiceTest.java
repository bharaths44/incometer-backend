package com.bharath.incometer.service;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.CategoryRequestDTO;
import com.bharath.incometer.entities.DTOs.CategoryResponseDTO;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.UsersRepository;
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
import java.util.UUID;

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
		user.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPhoneNumber("1234567890");

		category = new Category();
		category.setCategoryId(1L);
		category.setUser(user);
		category.setName("Food");
		category.setIcon("utensils");
		category.setType(TransactionType.EXPENSE);
		category.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void testAddCategory() {
		CategoryRequestDTO request = new CategoryRequestDTO(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                                    "Food",
		                                                    "utensils",
		                                                    TransactionType.EXPENSE);
		when(usersRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(
			user));
		when(categoryRepository.existsByUserUserIdAndNameAndType(UUID.fromString(
			"550e8400-e29b-41d4-a716" + "-446655440000"), "Food", TransactionType.EXPENSE)).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		System.out.println("Input: " + request);
		CategoryResponseDTO expected = new CategoryResponseDTO(1L,
		                                                       UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                                       "Food",
		                                                       "utensils",
		                                                       TransactionType.EXPENSE,
		                                                       category.getCreatedAt());
		System.out.println("Expected: " + expected);

		CategoryResponseDTO result = categoryService.addCategory(request);
		System.out.println("Real Output: " + result);

		assertEquals(expected.categoryId(), result.categoryId());
		assertEquals(expected.userId(), result.userId());
		assertEquals(expected.name(), result.name());
		assertEquals(expected.icon(), result.icon());
		assertEquals(expected.type(), result.type());
	}

	@Test
	void testGetCategoriesByUserId() {
		when(categoryRepository.findByUserUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(
			Collections.singletonList(category));

		System.out.println("Input: userId=1L");
		List<CategoryResponseDTO> expected = List.of(new CategoryResponseDTO(1L,
		                                                                     UUID.fromString(
			                                                                     "550e8400-e29b-41d4" + "-a716" +
			                                                                     "-446655440000"),
		                                                                     "Food",
		                                                                     "utensils",
		                                                                     TransactionType.EXPENSE,
		                                                                     category.getCreatedAt()));
		System.out.println("Expected: " + expected);

		List<CategoryResponseDTO> result = categoryService.getCategoriesByUserId(UUID.fromString(
			"550e8400-e29b-41d4-a716-446655440000"));
		System.out.println("Real Output: " + result);

		assertEquals(1, result.size());
		assertEquals("Food", result.getFirst().name());
		assertEquals("utensils", result.getFirst().icon());
	}

	@Test
	void testGetCategoryIdByName() {
		when(categoryRepository.findByUserUserIdAndNameIgnoreCase(UUID.fromString(
			"550e8400-e29b-41d4-a716" + "-446655440000"), "food")).thenReturn(category);

		System.out.println("Input: categoryName='food', userId=1L");
		Long expected = 1L;
		System.out.println("Expected: " + expected);

		Long result = categoryService.getCategoryIdByName("food",
		                                                  UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}

	@Test
	void testCreateCategoryForUser() {
		when(usersRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(
			user));
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		System.out.println("Input: categoryName='food', userId=1L, type=EXPENSE");
		Long expected = 1L;
		System.out.println("Expected: " + expected);

		Long result = categoryService.createCategoryForUser("food",
		                                                    UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
		                                                    TransactionType.EXPENSE);
		System.out.println("Real Output: " + result);

		assertEquals(expected, result);
	}
}
