package com.bharath.incometer;

import com.twilio.Twilio;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class IncometerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().systemProperties().load();
		System.setProperty("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
		System.setProperty("GEMINI_API_KEY", Objects.requireNonNull(dotenv.get("GEMINI_API_KEY")));
		System.setProperty("GOOGLE_CLIENT_ID", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_ID")));
		System.setProperty("GOOGLE_CLIENT_SECRET", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_SECRET")));
		System.setProperty("GOOGLE_REDIRECT_URI", Objects.requireNonNull(dotenv.get("GOOGLE_REDIRECT_URI")));
		System.setProperty("APP_AUTHORIZED_REDIRECT_URIS",
		                   Objects.requireNonNull(dotenv.get("APP_AUTHORIZED_REDIRECT_URIS")));
		System.setProperty("CORS_ALLOWED_ORIGINS", Objects.requireNonNull(dotenv.get("CORS_ALLOWED_ORIGINS")));
		System.setProperty("TWILIO_WHATSAPP_NUMBER", Objects.requireNonNull(dotenv.get("TWILIO_WHATSAPP_NUMBER")));
		Twilio.init(Objects.requireNonNull(dotenv.get("TWILIO_ACCOUNT_SID")),
		            Objects.requireNonNull(dotenv.get("TWILIO_AUTH_TOKEN")));
		SpringApplication.run(IncometerApplication.class, args);
	}
}
