package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class CategoryRepositoryTest {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UsersRepository usersRepository;

	private Users user;

	@BeforeEach
	void setUp() {
		user = createUser();
		createCategory(user);
	}

	private Users createUser() {
		Users u = new Users();
		u.setName("Test User");
		u.setEmail("test@example.com");
		u.setPhoneNumber("1234567890");

		return usersRepository.save(u);
	}

	private void createCategory(Users user) {
		Category c = new Category();
		c.setUser(user);
		c.setName("Food");
		c.setIcon("utensils");
		c.setType(TransactionType.EXPENSE);
		categoryRepository.save(c);
	}

	@Test
	void testFindByUserUserId() {
		List<Category> result = categoryRepository.findByUserUserId(user.getUserId());

		assertEquals(1, result.size());
		assertEquals("Food", result.getFirst().getName());
	}

	@Test
	void testExistsByUserUserIdAndNameAndType() {
		boolean exists = categoryRepository.existsByUserUserIdAndNameAndType(user.getUserId(),
		                                                                     "Food",
		                                                                     TransactionType.EXPENSE);

		assertTrue(exists);
	}

	@Test
	void testFindByUserUserIdAndNameIgnoreCase() {
		Category result = categoryRepository.findByUserUserIdAndNameIgnoreCase(user.getUserId(), "food");

		assertNotNull(result);
		assertEquals("Food", result.getName());
	}
}

