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

		// Skip if authentication already exists (handles concurrent requests)
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			filterChain.doFilter(request, response);
			return;
		}

		// Extract tokens from cookies
		String accessToken = null;
		String refreshToken = null;

		for (Cookie cookie : cookies) {
			if ("accessToken".equals(cookie.getName())) {
				accessToken = cookie.getValue();
			} else if ("refreshToken".equals(cookie.getName())) {
				refreshToken = cookie.getValue();
			}
		}

		// Try to authenticate with accessToken
		if (accessToken != null) {
			try {
				String userEmail = jwtService.extractUsername(accessToken);
				if (userEmail != null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
					if (jwtService.isTokenValid(accessToken, userDetails)) {
						UsernamePasswordAuthenticationToken authToken =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
						filterChain.doFilter(request, response);
						return;
					}
				}
			} catch (Exception e) {
				// Access token invalid, try refresh
			}
		}

		// If no valid accessToken, try to refresh using refreshToken
		if (refreshToken != null) {
			try {
				String userEmail = jwtService.extractUsername(refreshToken);
				if (userEmail != null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
					if (jwtService.isTokenValid(refreshToken, userDetails)) {
						// Generate new access token
						Users userEntity = usersRepository.findByEmail(userEmail)
							.orElseThrow(() -> new RuntimeException("User not found"));
						String newAccessToken = jwtService.generateToken(userEntity);

						// Set new access token cookie
						ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
							.httpOnly(true)
							.secure(false) // Set to true in production with HTTPS
							.path("/")
							.maxAge(24 * 60 * 60) // 24 hours
							.sameSite("Lax")
							.build();
						response.addHeader("Set-Cookie", accessCookie.toString());

						// Set authentication for this request
						UsernamePasswordAuthenticationToken authToken =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				}
			} catch (Exception e) {
				// Refresh failed, proceed without authentication
			}
		}

		filterChain.doFilter(request, response);
	}
}
