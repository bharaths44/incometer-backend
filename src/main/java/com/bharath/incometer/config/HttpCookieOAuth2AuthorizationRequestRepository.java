package com.bharath.incometer.config;

import com.bharath.incometer.utils.CookieUtils;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	private static final Logger logger = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	private static final int cookieExpireSeconds = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return (OAuth2AuthorizationRequest) CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
		                                               .map(cookie -> {
			                                               try {
				                                               return CookieUtils.deserialize(cookie);
			                                               } catch (Exception e) {
				                                               logger.warn(
					                                               "Failed to deserialize OAuth2 authorization " +
					                                               "request" +
					                                               " cookie. Cookie may be stale " + "or " +
					                                               "corrupted.",
					                                               e);
				                                               return null;
			                                               }
		                                               })
		                                               .orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
	                                     HttpServletResponse response) {
		if (authorizationRequest == null) {
			CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
			CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
			return;
		}

		CookieUtils.addCookie(response,
		                      OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
		                      CookieUtils.serialize(authorizationRequest),
		                      cookieExpireSeconds);
		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
			CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin,
			                      cookieExpireSeconds);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
	                                                             HttpServletResponse response) {
		OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
		// Always clear cookies, even if authRequest is null (handles corrupted cookies)
		this.removeAuthorizationRequestCookies(request, response);
		return authRequest;
	}

	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
	}
}
