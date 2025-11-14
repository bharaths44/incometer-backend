package com.bharath.incometer.config;

import com.bharath.incometer.repository.UsersRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

	private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
	private final UsersRepository repository;

	@Value("${jwt.secret}")
	private String secretKey;

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> repository.findByEmail(username)
		                             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		byte[] keyBytes = secretKey == null ? new byte[0] : secretKey.trim().getBytes(StandardCharsets.UTF_8);
		SecretKey keyToUse;
		if (keyBytes.length < 32) { // HS256 requires >= 32 bytes (256 bits)
			log.warn(
				"Configured jwt.secret is too short ({} bytes). Generating ephemeral HS256 key for runtime. PLEASE " +
				"set" + " a secure >=32 byte secret in the environment/property for non-test profiles.",
				keyBytes.length);
			keyToUse = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		} else {
			keyToUse = Keys.hmacShaKeyFor(keyBytes);
		}
		return NimbusJwtDecoder.withSecretKey(keyToUse).build();
	}
}
