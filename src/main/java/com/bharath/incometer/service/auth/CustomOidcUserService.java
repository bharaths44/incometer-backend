package com.bharath.incometer.service.auth;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.exceptions.OAuth2AuthenticationProcessingException;
import com.bharath.incometer.models.user.OAuth2UserInfo;
import com.bharath.incometer.models.user.OAuth2UserInfoFactory;
import com.bharath.incometer.models.user.UserPrincipal;
import com.bharath.incometer.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.bharath.incometer.service.auth.CustomOAuth2UserService.saveUsers;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

	private final UsersRepository userRepository;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		System.out.println(
			"Loading OIDC user for provider: " + userRequest.getClientRegistration().getRegistrationId());
		OidcUser oidcUser = super.loadUser(userRequest);
		System.out.println("OIDC user loaded: " + oidcUser.getAttributes());

		try {
			return processOidcUser(userRequest, oidcUser);
		} catch (AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
		}
	}

	private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(userRequest.getClientRegistration()
		                                                                                   .getRegistrationId(),
		                                                                        oidcUser.getAttributes());

		if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
			throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
		}

		Optional<Users> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
		Users user;

		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!user.getProvider()
			         .equals(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId()))) {
				throw new OAuth2AuthenticationProcessingException(
					"Looks like you're signed up with " + user.getProvider() + " account. Please use your " +
					user.getProvider() + " account to login.");
			}
			user = updateExistingUser(user, oAuth2UserInfo);
		} else {
			user = registerNewUser(userRequest, oAuth2UserInfo);
		}

		return UserPrincipal.create(user, oidcUser.getAttributes());
	}

	private Users registerNewUser(OidcUserRequest userRequest, OAuth2UserInfo oAuth2UserInfo) {
		return saveUsers(userRequest, oAuth2UserInfo, userRepository);
	}

	private Users updateExistingUser(Users existingUser, OAuth2UserInfo oAuth2UserInfo) {
		existingUser.setName(oAuth2UserInfo.getName());
		return userRepository.save(existingUser);
	}
}
