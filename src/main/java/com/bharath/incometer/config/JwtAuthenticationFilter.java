package com.bharath.incometer.config;

import com.bharath.incometer.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bharath.incometer.repository.UsersRepository;
import com.bharath.incometer.entities.Users;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final UsersRepository usersRepository;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String requestPath = request.getRequestURI();
		System.out.println("=== JWT Filter Start === Path: " + requestPath);

		final String jwt;
		final String userEmail;

		String token = null;

		// First, try Authorization header (priority for frontend apps)
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			System.out.println("✓ Token found in Authorization header");
		}

		// If no Authorization header, try to get token from accessToken cookie
		if (token == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				System.out.println("Found " + cookies.length + " cookies");
				for (Cookie cookie : cookies) {
					System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
					if ("accessToken".equals(cookie.getName())) {
						token = cookie.getValue();
						System.out.println("✓ AccessToken found in cookies");
						break;
					}
				}

				// Only try refresh if we have cookies but no accessToken
				if (token == null) {
					// Try to refresh using refreshToken
					String refreshToken = null;
					for (Cookie cookie : cookies) {
						if ("refreshToken".equals(cookie.getName())) {
							refreshToken = cookie.getValue();
							System.out.println("✓ RefreshToken found in cookies");
							break;
						}
					}
					if (refreshToken != null) {
						try {
							String username = jwtService.extractUsername(refreshToken);
							var userDetails = userDetailsService.loadUserByUsername(username);
							if (jwtService.isTokenValid(refreshToken, userDetails)) {
								System.out.println("✓ RefreshToken is valid, generating new access token");
								Users userEntity = usersRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
								String newAccessToken = jwtService.generateToken(userEntity);

								// Set new access token cookie
								ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
								                                            .httpOnly(true)
								                                            .secure(false) // Set to true in production
								                                            .path("/")
								                                            .maxAge(24 * 60 * 60) // 24 hours
								                                            .sameSite("Lax")
								                                            .build();
								response.addHeader("Set-Cookie", accessCookie.toString());

								// Set authentication for this request
								UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
								                                                                                        null,
								                                                                                        userDetails.getAuthorities());
								authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
								SecurityContextHolder.getContext().setAuthentication(authToken);
								System.out.println("✓ New access token generated and authentication set for user: " + username);
								filterChain.doFilter(request, response);
								return;
							} else {
								System.out.println("❌ RefreshToken validation failed");
							}
						} catch (Exception e) {
							System.out.println("❌ Error during token refresh: " + e.getMessage());
						}
					}
				}
			} else {
				System.out.println("No cookies found in request");
			}
		}

		if (token == null) {
			System.out.println("❌ No token found - proceeding without authentication");
			filterChain.doFilter(request, response);
			return;
		}

		jwt = token;
		System.out.println("Extracting username from token...");
		userEmail = jwtService.extractUsername(jwt);
		System.out.println("Username extracted: " + userEmail);

		if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			System.out.println("Loading user details for: " + userEmail);
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
			System.out.println("User details loaded, validating token...");

			if (jwtService.isTokenValid(jwt, userDetails)) {
				System.out.println("✓ Token is valid, setting authentication");
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
				                                                                                        null,
				                                                                                        userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
				System.out.println("✓ Authentication set successfully for user: " + userEmail);
			} else {
				System.out.println("❌ Token validation failed for user: " + userEmail);
			}
		} else if (userEmail == null) {
			System.out.println("❌ Could not extract username from token");
		} else {
			System.out.println("Authentication already exists in context");
		}

		System.out.println("=== JWT Filter End ===\n");
		filterChain.doFilter(request, response);
	}
}
