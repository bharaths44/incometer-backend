package com.bharath.incometer.config;

import com.bharath.incometer.models.user.UserPrincipal;
import com.bharath.incometer.service.auth.JwtService;
import com.bharath.incometer.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.bharath.incometer.config.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;

	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Value("${app.oauth2.authorizedRedirectUris}")
	private String redirectUri;

	@Value("${app.cookie.secure:true}")
	private boolean cookieSecure;

	@Value("${app.cookie.sameSite:Lax}")
	private String cookieSameSite;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	                                    Authentication authentication) throws IOException {
		System.out.println("=== OAUTH2 SUCCESS START ===");
		System.out.println("OAuth2 authentication success for user: " + authentication.getName());
		System.out.println("Principal type: " + authentication.getPrincipal().getClass().getName());
		String targetUrl = determineTargetUrl(request, response, authentication);
		System.out.println("Target redirect URL: " + targetUrl);

		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
			System.out.println("❌ Response already committed, cannot redirect");
			return;
		}

		clearAuthenticationAttributes(request, response);
		System.out.println("✓ Redirecting to: " + targetUrl);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
		System.out.println("=== OAUTH2 SUCCESS END ===\n");
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
	                                    Authentication authentication) {
		System.out.println("--- Determining Target URL ---");
		Optional<String> redirectUriFromCookie = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
		                                                    .map(Cookie::getValue);

		// Use cookie value if present, otherwise use configured default
		String targetUrl;
		if (redirectUriFromCookie.isPresent()) {
			System.out.println("Redirect URI found in cookie: " + redirectUriFromCookie.get());
			if (!isAuthorizedRedirectUri(redirectUriFromCookie.get())) {
				System.out.println("❌ Unauthorized redirect URI: " + redirectUriFromCookie.get());
				throw new RuntimeException(
					"Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
			}
			targetUrl = redirectUriFromCookie.get();
		} else {
			// Use the configured redirect URI from application.yml
			targetUrl = this.redirectUri;
			System.out.println("No redirect_uri cookie found, using default: " + targetUrl);
		}

		// Extract UserPrincipal - handle both direct UserPrincipal and wrapped OAuth2User
		UserPrincipal userPrincipal;
		Object principal = authentication.getPrincipal();
		System.out.println("Extracting user principal from: " + principal.getClass().getSimpleName());

		if (principal instanceof UserPrincipal) {
			userPrincipal = (UserPrincipal) principal;
			System.out.println("✓ UserPrincipal extracted - User ID: " + userPrincipal.getId());
		} else {
			System.out.println("❌ Unexpected principal type: " + principal.getClass().getName());
			throw new RuntimeException("Unexpected principal type: " + principal.getClass().getName());
		}

		System.out.println("Generating tokens for user: " + userPrincipal.getUsername());
		String token = jwtService.generateToken(userPrincipal);
		String refreshToken = jwtService.generateRefreshToken(userPrincipal);
		System.out.println(
			"✓ Tokens generated - Access: " + token.length() + " chars, Refresh: " + refreshToken.length() + " chars");
		System.out.println("Cookie secure setting: " + cookieSecure);

		// Create access token cookie with all security attributes
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", token)
		                                            .httpOnly(true)           // Cannot be accessed by JavaScript
		                                            .secure(cookieSecure)     // Only sent over HTTPS (configurable
		                                            // for dev/prod)
		                                            .path("/")                // Available for all paths
		                                            .maxAge(15 * 60)          // 15 minutes
		                                            .sameSite(cookieSameSite)          // CSRF protection
		                                            .build();

		// Create refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
		                                             .httpOnly(true)
		                                             .secure(cookieSecure)
		                                             .path("/")
		                                             .maxAge(7 * 24 * 60 * 60) // 7 days
		                                             .sameSite(cookieSameSite)
		                                             .build();

		System.out.println("Access cookie: " + accessCookie);
		System.out.println("Refresh cookie: " + refreshCookie);

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

		System.out.println("✓ OAuth2 cookies added to response headers");
		System.out.println("--- End Determining Target URL ---");

		return targetUrl;
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);
		URI authorizedURI = URI.create(redirectUri);

		return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost()) &&
		       authorizedURI.getPort() == clientRedirectUri.getPort();
	}
}
