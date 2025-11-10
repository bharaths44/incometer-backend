package com.bharath.incometer.service.auth;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.AuthProvider;
import com.bharath.incometer.enums.Role;
import com.bharath.incometer.exceptions.OAuth2AuthenticationProcessingException;
import com.bharath.incometer.models.user.OAuth2UserInfo;
import com.bharath.incometer.models.user.OAuth2UserInfoFactory;
import com.bharath.incometer.models.user.UserPrincipal;
import com.bharath.incometer.repository.UsersRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UsersRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
		System.out.println(
			"Loading OAuth2 user for provider: " + oAuth2UserRequest.getClientRegistration().getRegistrationId());
		OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
		System.out.println("OAuth2 user loaded: " + oAuth2User.getAttributes());

		try {
			return processOAuth2User(oAuth2UserRequest, oAuth2User);
		} catch (AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			// Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
		}
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
		OAuth2UserInfo oAuth2UserInfo =
			OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration()
			                                                         .getRegistrationId(),
			                                        oAuth2User.getAttributes());
		if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
			throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
		}

		Optional<Users> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
		Users user;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!user.getProvider()
			         .equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
				throw new OAuth2AuthenticationProcessingException(
					"Looks like you're signed up with " + user.getProvider() + " account. Please use your " +
					user.getProvider() + " account to login.");
			}
			user = updateExistingUser(user, oAuth2UserInfo);
		} else {
			user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
		}

		return UserPrincipal.create(user, oAuth2User.getAttributes());
	}

	private Users registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
		return saveUsers(oAuth2UserRequest, oAuth2UserInfo, userRepository);
	}

	@NotNull
	static Users saveUsers(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo,
	                       UsersRepository userRepository) {
		Users user = new Users();

		user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
		user.setName(oAuth2UserInfo.getName());
		user.setEmail(oAuth2UserInfo.getEmail());
		user.setRole(Role.USER);
		return userRepository.save(user);
	}

	private Users updateExistingUser(Users existingUser, OAuth2UserInfo oAuth2UserInfo) {
		existingUser.setName(oAuth2UserInfo.getName());
		return userRepository.save(existingUser);
	}

}
