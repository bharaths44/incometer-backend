package com.bharath.incometer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
		        .allowedOrigins("https://k7hff2hr-5173.inc1.devtunnels.ms",
		                        "https://k7hff2hr-8080.inc1.devtunnels.ms",
		                        "http://localhost:4173",
		                        "http://localhost:5173"

		                       )
		        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
		        .allowedHeaders("*")
		        .allowCredentials(true)
		        .maxAge(3600);
	}
}
