package com.bharath.incometer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
		        .allowedOrigins("https://k7hff2hr-5173.inc1.devtunnels.ms",
		                        "https://k7hff2hr-8080.inc1.devtunnels.ms",
		                        "http://localhost:3000"

		                       )
		        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
		        .allowedHeaders("*")
		        .exposedHeaders("Set-Cookie")
		        .allowCredentials(true)
		        .maxAge(3600);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("https://k7hff2hr-5173.inc1.devtunnels.ms",
		                                              "https://k7hff2hr-8080.inc1.devtunnels.ms",
		                                              "http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Set-Cookie"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
