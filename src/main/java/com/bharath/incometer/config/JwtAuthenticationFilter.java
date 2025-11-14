package com.bharath.incometer.config;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final UsersRepository usersRepository;

	// Track ongoing refresh operations per user to prevent concurrent refreshes
	private final ConcurrentHashMap<String, Object> refreshLocks = new ConcurrentHashMap<>();

	@Value("${app.cookie.secure:true}")
	private boolean cookieSecure;

	@Value("${app.cookie.sameSite:Lax}")
	private String cookieSameSite;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		System.out.println("=== JWT FILTER START ===");
		System.out.println("Request URI: " + request.getRequestURI());
		System.out.println("Request Method: " + request.getMethod());

		// Skip if authentication already exists (handles concurrent requests)
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			System.out.println("✓ Authentication already exists, skipping filter");
			System.out.println("=== JWT FILTER END ===\n");
			filterChain.doFilter(request, response);
			return;
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			System.out.println("No cookies found in request");
			System.out.println("=== JWT FILTER END ===\n");
			filterChain.doFilter(request, response);
			return;
		}

		// Extract tokens from cookies
		String accessToken = null;
		String refreshToken = null;

		System.out.println("--- Extracting Tokens from Cookies ---");
		for (Cookie cookie : cookies) {
			if ("accessToken".equals(cookie.getName())) {
				accessToken = cookie.getValue();
				System.out.println("✓ Access token found (length: " + accessToken.length() + ")");
			} else if ("refreshToken".equals(cookie.getName())) {
				refreshToken = cookie.getValue();
				System.out.println("✓ Refresh token found (length: " + refreshToken.length() + ")");
			}
		}

		// Try to authenticate with accessToken
		if (accessToken != null) {
			System.out.println("--- Attempting Access Token Authentication ---");
			try {
				String userEmail = jwtService.extractUsername(accessToken);
				System.out.println("Extracted email from access token: " + userEmail);
				if (userEmail != null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
					System.out.println("User loaded: " + userDetails.getUsername());
					if (jwtService.isTokenValid(accessToken, userDetails)) {
						System.out.println("✓ Access token is valid");
						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
						System.out.println("✓ Authentication set in SecurityContext");
						System.out.println("=== JWT FILTER END ===\n");
						filterChain.doFilter(request, response);
						return;
					} else {
						System.out.println("❌ Access token is invalid or expired");
					}
				}
			} catch (Exception e) {
				System.out.println("❌ Access token validation failed: " + e.getMessage());
			}
		} else {
			System.out.println("No access token found in cookies");
		}

		// If no valid accessToken, try to refresh using refreshToken
		if (refreshToken != null) {
			System.out.println("--- Attempting Token Refresh ---");
			try {
				String userEmail = jwtService.extractUsername(refreshToken);
				System.out.println("Extracted email from refresh token: " + userEmail);

				if (userEmail != null) {
					// Use computeIfAbsent to atomically get or create a lock object for this user
					Object userLock = refreshLocks.computeIfAbsent(userEmail, k -> new Object());

					System.out.println("🔒 Acquired lock for user: " + userEmail);
					synchronized (userLock) {
						System.out.println("--- Inside Synchronized Block ---");

						// Double-check if another thread already refreshed the token
						if (SecurityContextHolder.getContext().getAuthentication() != null) {
							System.out.println("✓ Authentication already set by another thread, skipping refresh");
							System.out.println("=== JWT FILTER END ===\n");
							filterChain.doFilter(request, response);
							return;
						}

						UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
						System.out.println("User loaded: " + userDetails.getUsername());

						if (jwtService.isTokenValid(refreshToken, userDetails)) {
							System.out.println("✓ Refresh token is valid");

							// Generate new access token
							Users userEntity = usersRepository.findByEmail(userEmail)
							                                  .orElseThrow(() -> new RuntimeException(
								                                  "User not " + "found"));
							System.out.println("Generating new access token for user ID: " + userEntity.getUserId());
							String newAccessToken = jwtService.generateToken(userEntity);
							System.out.println(
								"✓ New access token generated (length: " + newAccessToken.length() + ")");

							// Set new access token cookie with 15 minutes expiration
							System.out.println("--- Setting New Access Token Cookie ---");
							System.out.println("Cookie secure setting: " + cookieSecure);
							System.out.println("Cookie sameSite setting: " + cookieSameSite);
							ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
							                                            .httpOnly(true)
							                                            .secure(cookieSecure)
							                                            .path("/")
							                                            .maxAge(15 * 60) // 15 minutes
							                                            .sameSite(cookieSameSite)
							                                            .build();
							response.addHeader("Set-Cookie", accessCookie.toString());
							System.out.println("✓ New access token cookie set");

							// Set authentication for this request
							UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								userDetails,
								null,
								userDetails.getAuthorities());
							authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(authToken);
							System.out.println("✓ Authentication set in SecurityContext after token refresh");
						} else {
							System.out.println("❌ Refresh token is invalid or expired");
						}
					}

					// Clean up the lock after use to prevent memory leaks
					refreshLocks.remove(userEmail);
					System.out.println("🔓 Released and removed lock for user: " + userEmail);
				}
			} catch (Exception e) {
				System.out.println("❌ Token refresh failed: " + e.getMessage());
			}
		} else {
			System.out.println("No refresh token found in cookies");
		}

		System.out.println("=== JWT FILTER END ===\n");
		filterChain.doFilter(request, response);
	}
}
