package com.bharath.incometer.service;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.models.user.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

	private JwtService jwtService;
	private Users testUser;
	private UserPrincipal testUserPrincipal;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();

		// Set up test properties
		ReflectionTestUtils.setField(jwtService, "secretKey", "mySecretKeyForJWTWhichShouldBeLongEnoughToBeSecure");
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 hours
		ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days

		// Create test user
		testUser = new Users();
		testUser.setUserId(UUID.randomUUID());
		testUser.setEmail("test@example.com");
		testUser.setName("Test User");

		// Create test user principal
		testUserPrincipal = UserPrincipal.create(testUser);
	}

	@Test
	void shouldGenerateTokenForUser() {
		// When
		String token = jwtService.generateToken(testUser);

		// Then
		assertNotNull(token);
		assertFalse(token.isEmpty());
		assertTrue(token.contains("."));
	}

	@Test
	void shouldGenerateTokenForUserPrincipal() {
		// When
		String token = jwtService.generateToken(testUserPrincipal);

		// Then
		assertNotNull(token);
		assertFalse(token.isEmpty());
		assertTrue(token.contains("."));
	}

	@Test
	void shouldExtractUsernameFromToken() {
		// Given
		String token = jwtService.generateToken(testUser);

		// When
		String username = jwtService.extractUsername(token);

		// Then
		assertEquals(testUser.getEmail(), username);
	}

	@Test
	void shouldValidateValidToken() {
		// Given
		String token = jwtService.generateToken(testUser);

		// When & Then
		assertTrue(jwtService.isTokenValid(token, testUserPrincipal));
	}

	@Test
	void shouldInvalidateTokenWithWrongUser() {
		// Given
		String token = jwtService.generateToken(testUser);
		Users wrongUser = new Users();
		wrongUser.setUserId(UUID.randomUUID());
		wrongUser.setEmail("wrong@example.com");
		UserPrincipal wrongUserPrincipal = UserPrincipal.create(wrongUser);

		// When & Then
		assertFalse(jwtService.isTokenValid(token, wrongUserPrincipal));
	}

	@Test
	void shouldExtractUuidFromToken() {
		// Given
		String token = jwtService.generateToken(testUser);

		// When
		String uuid = jwtService.extractClaim(token, claims -> claims.get("uuid", String.class));

		// Then
		assertEquals(testUser.getUserId().toString(), uuid);
	}

	@Test
	void shouldGenerateRefreshTokenForUser() {
		// When
		String refreshToken = jwtService.generateRefreshToken(testUser);

		// Then
		assertNotNull(refreshToken);
		assertFalse(refreshToken.isEmpty());
		assertTrue(refreshToken.contains("."));
		assertTrue(jwtService.isTokenValid(refreshToken, testUserPrincipal));
	}
}