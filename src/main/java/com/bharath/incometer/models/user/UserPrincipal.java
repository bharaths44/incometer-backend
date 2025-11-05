package com.bharath.incometer.models.user;

import com.bharath.incometer.entities.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class UserPrincipal implements OAuth2User, OidcUser, UserDetails {
	@Getter
	private final UUID id;
	@Getter
	private final String email;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;
	@Setter
	private Map<String, Object> attributes;
	@Setter
	private Map<String, Object> claims;

	public UserPrincipal(UUID id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
	}

	public static UserPrincipal create(Users user) {
		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

		return new UserPrincipal(user.getUserId(), user.getEmail(), user.getPassword(), authorities);
	}

	public static UserPrincipal create(Users user, Map<String, Object> attributes) {
		UserPrincipal userPrincipal = UserPrincipal.create(user);
		userPrincipal.setAttributes(attributes);
		return userPrincipal;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return String.valueOf(id);
	}

	// OidcUser specific methods
	@Override
	public Map<String, Object> getClaims() {
		return claims != null ? claims : attributes;
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return null; // Not needed for JWT generation
	}

	@Override
	public OidcIdToken getIdToken() {
		return null; // Not needed for JWT generation
	}
}
