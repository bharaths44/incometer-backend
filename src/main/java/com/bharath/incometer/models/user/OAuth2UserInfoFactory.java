package com.bharath.incometer.models.user;

import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.exceptions.OAuth2AuthenticationProcessingException;

import java.util.Map;

public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
		if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
			return new GoogleOAuth2UserInfo(attributes);
		} else {
			throw new OAuth2AuthenticationProcessingException(
				"Sorry! Login with " + registrationId + " is not supported yet.");
		}
	}
}

