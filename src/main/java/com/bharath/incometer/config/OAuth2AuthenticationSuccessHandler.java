package com.bharath.incometer.config;

import com.bharath.incometer.models.user.UserPrincipal;
import com.bharath.incometer.service.JwtService;
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

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
	                                    Authentication authentication) throws IOException {
		System.out.println("OAuth2 authentication success for user: " + authentication.getName());
		String targetUrl = determineTargetUrl(request, response, authentication);
		System.out.println("Redirecting to: " + targetUrl);

		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
			return;
		}

		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
	                                    Authentication authentication) {
		Optional<String> redirectUriFromCookie = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
		                                                    .map(Cookie::getValue);

		// Use cookie value if present, otherwise use configured default
		String targetUrl;
		if (redirectUriFromCookie.isPresent()) {
			if (!isAuthorizedRedirectUri(redirectUriFromCookie.get())) {
				throw new RuntimeException(
					"Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
			}
			targetUrl = redirectUriFromCookie.get();
		} else {
			// Use the configured redirect URI from application.yml
			targetUrl = this.redirectUri;
			logger.info("No redirect_uri cookie found, using default: " + targetUrl);
		}

		// Extract UserPrincipal - handle both direct UserPrincipal and wrapped OAuth2User
		UserPrincipal userPrincipal;
		Object principal = authentication.getPrincipal();

		if (principal instanceof UserPrincipal) {
			userPrincipal = (UserPrincipal) principal;
		} else {
			throw new RuntimeException("Unexpected principal type: " + principal.getClass().getName());
		}

		String token = jwtService.generateToken(userPrincipal);
		String refreshToken = jwtService.generateRefreshToken(userPrincipal);

		// Create access token cookie with all security attributes
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", token)
			.httpOnly(true)           // Cannot be accessed by JavaScript
			.secure(cookieSecure)     // Only sent over HTTPS (configurable for dev/prod)
			.path("/")                // Available for all paths
			.maxAge(24 * 60 * 60)     // 24 hours
			.sameSite("Lax")          // CSRF protection
			.build();

		// Create refresh token cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(cookieSecure)
			.path("/")
			.maxAge(7 * 24 * 60 * 60) // 7 days
			.sameSite("Lax")
			.build();

		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());

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
