package com.bharath.incometer;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class IncometerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
		                      .ignoreIfMissing()
		                      .systemProperties()
		                      .load();
		System.setProperty("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
		System.setProperty("GEMINI_API_KEY", Objects.requireNonNull(dotenv.get("GEMINI_API_KEY")));
		System.setProperty("GOOGLE_CLIENT_ID", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_ID")));
		System.setProperty("GOOGLE_CLIENT_SECRET", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_SECRET")));
		System.setProperty("GOOGLE_REDIRECT_URI", Objects.requireNonNull(dotenv.get("GOOGLE_REDIRECT_URI")));
		System.setProperty("APP_AUTHORIZED_REDIRECT_URIS", Objects.requireNonNull(dotenv.get("APP_AUTHORIZED_REDIRECT_URIS")));
		SpringApplication.run(IncometerApplication.class, args);
	}
}
