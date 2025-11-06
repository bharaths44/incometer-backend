package com.bharath.incometer.repository;

import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Users;
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
public class PaymentMethodRepositoryTest {

	@Autowired
	private PaymentMethodRepository paymentMethodRepository;

	@Autowired
	private UsersRepository usersRepository;

	private Users testUser;

	@BeforeEach
	void setUp() {
		// Create and save test user
		testUser = new Users();
		testUser.setName("Test User");
		testUser.setEmail("test@example.com");
		testUser.setPhoneNumber("1234567890");
		testUser = usersRepository.save(testUser);
	}

	@Test
	void shouldSaveAndFindPaymentMethod() {
		// Given
		PaymentMethod paymentMethod = new PaymentMethod();
		paymentMethod.setName("Credit Card");
		paymentMethod.setUser(testUser);

		// When
		PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

		// Then
		assertNotNull(saved.getPaymentMethodId());
		assertEquals("Credit Card", saved.getName());
		assertEquals(testUser.getUserId(), saved.getUser().getUserId());
	}

	@Test
	void shouldFindPaymentMethodsByUserId() {
		// Given
		PaymentMethod cash = createPaymentMethod("Cash");
		PaymentMethod card = createPaymentMethod("Credit Card");
		paymentMethodRepository.save(cash);
		paymentMethodRepository.save(card);

		// When
		List<PaymentMethod> userPaymentMethods = paymentMethodRepository.findByUserUserId(testUser.getUserId());

		// Then
		assertEquals(2, userPaymentMethods.size());
		assertTrue(userPaymentMethods.stream().allMatch(pm -> pm.getUser().getUserId().equals(testUser.getUserId())));
		assertTrue(userPaymentMethods.stream().anyMatch(pm -> pm.getName().equals("Cash")));
		assertTrue(userPaymentMethods.stream().anyMatch(pm -> pm.getName().equals("Credit Card")));
	}

	@Test
	void shouldReturnEmptyListForUserWithNoPaymentMethods() {
		// Given - no payment methods created

		// When
		List<PaymentMethod> userPaymentMethods = paymentMethodRepository.findByUserUserId(testUser.getUserId());

		// Then
		assertTrue(userPaymentMethods.isEmpty());
	}

	@Test
	void shouldDeletePaymentMethod() {
		// Given
		PaymentMethod paymentMethod = paymentMethodRepository.save(createPaymentMethod("Cash"));
		assertEquals(1, paymentMethodRepository.findByUserUserId(testUser.getUserId()).size());

		// When
		paymentMethodRepository.deleteById(paymentMethod.getPaymentMethodId());

		// Then
		assertTrue(paymentMethodRepository.findById(paymentMethod.getPaymentMethodId()).isEmpty());
		assertEquals(0, paymentMethodRepository.findByUserUserId(testUser.getUserId()).size());
	}

	private PaymentMethod createPaymentMethod(String name) {
		PaymentMethod paymentMethod = new PaymentMethod();
		paymentMethod.setName(name);
		paymentMethod.setType(com.bharath.incometer.enums.PaymentType.CASH);
		paymentMethod.setUser(testUser);
		return paymentMethod;
	}
}