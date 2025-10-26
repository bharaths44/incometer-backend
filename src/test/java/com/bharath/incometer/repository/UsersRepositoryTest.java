package com.bharath.incometer.repository;

import com.bharath.incometer.entities.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UsersRepositoryTest {

	@Autowired
	private UsersRepository usersRepository;

	@BeforeEach
	void setUp() {
		createUser();
	}

	private void createUser() {
		Users u = new Users();
		u.setName("Test User");
		u.setEmail("test@example.com");
		u.setPhoneNumber("1234567890");
		u.setPassword("password");
		usersRepository.save(u);
	}

	@Test
	void testExistsByEmail() {
		boolean exists = usersRepository.existsByEmail("test@example.com");
		assertTrue(exists);
	}

	@Test
	void testFindByPhoneNumber() {
		Users result = usersRepository.findByPhoneNumber("1234567890");
		assertNotNull(result);
		assertEquals("Test User", result.getName());
	}

	@Test
	void testExistsByPhoneNumber() {
		boolean exists = usersRepository.existsByPhoneNumber("1234567890");
		assertTrue(exists);
	}
}