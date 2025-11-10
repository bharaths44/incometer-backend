package com.bharath.incometer.config;

import com.bharath.incometer.service.auth.CustomOAuth2UserService;
import com.bharath.incometer.service.auth.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**", "/oauth2/**", "/v2/api-docs", "/v3/api-docs",
	                                                "/v3/api-docs/**", "/swagger-resources", "/swagger-resources/**",
	                                                "/configuration/ui", "/configuration/security", "/swagger-ui/**",
	                                                "/webjars/**", "/swagger-ui.html"};
	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomOidcUserService customOidcUserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
		    .cors(cors -> cors.configurationSource(corsConfigurationSource))
		    .authorizeHttpRequests(req -> req.requestMatchers(WHITE_LIST_URL).permitAll().anyRequest().authenticated())
		    .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
		    .authenticationProvider(authenticationProvider)
		    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
		    .oauth2Login(oauth2 -> oauth2.authorizationEndpoint(authorization -> authorization.baseUri(
			                                 "/oauth2/authorize").authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
		                                 .redirectionEndpoint(redirection -> redirection.baseUri("/oauth2/callback/*"))
		                                 .userInfoEndpoint(userInfo -> userInfo
		                                         .userService(customOAuth2UserService)
		                                         .oidcUserService(customOidcUserService))
		                                 .successHandler(oAuth2AuthenticationSuccessHandler)
		                                 .failureHandler(oAuth2AuthenticationFailureHandler))
		    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
		    .logout(logout -> logout.logoutUrl("/api/v1/auth/logout").logoutSuccessUrl("/").permitAll());

		return http.build();
	}
}
