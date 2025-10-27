package com.bharath.incometer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
		        .allowedOriginPatterns(
			        "https://expensebot.loca.lt",
			        "http://localhost:*",
			        "https://localhost:*",
			        "http://127.0.0.1:*",
			        "https://127.0.0.1:*"
		                              )
		        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
		        .allowedHeaders("*")
		        .allowCredentials(true)
		        .maxAge(3600);
	}
}
