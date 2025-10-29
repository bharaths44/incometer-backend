package com.bharath.incometer;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class IncometerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
		System.setProperty("GEMINI_API_KEY", Objects.requireNonNull(dotenv.get("GEMINI_API_KEY")));
		SpringApplication.run(IncometerApplication.class, args);
	}

//	@Bean
//	@Profile("!test")
//	public CommandLineRunner startLocalTunnel() {
//		return args -> {
//			System.out.println("🌐 CommandLineRunner running! Starting LocalTunnel...");
//
//			try (var executor = Executors.newSingleThreadExecutor()) {
//				executor.submit(() -> {
//					try {
//						String os = System.getProperty("os.name").toLowerCase();
//						String command = "npx lt --port 8080 --subdomain expensebot";
//
//						ProcessBuilder pb = os.contains("win")
//						                    ? new ProcessBuilder("cmd.exe", "/c", command)
//						                    : new ProcessBuilder("sh", "-c", command);
//
//						// Ensure PATH is inherited so Node/NPM is found
//						pb.environment().put("PATH", System.getenv("PATH"));
//						pb.redirectErrorStream(true);
//
//						System.out.println("📦 Executing: " + String.join(" ", pb.command()));
//
//						Process process = pb.start();
//
//						try (BufferedReader reader =
//							     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//							String line;
//							while ((line = reader.readLine()) != null) {
//								System.out.println("🌍 " + line);
//							}
//						}
//
//						int exitCode = process.waitFor();
//						if (exitCode != 0) {
//							System.out.println("❌ LocalTunnel failed with exit code: " + exitCode);
//							System.out.println(
//								"💡 Ensure Node.js and LocalTunnel are installed. Alternatively, expose port 8080 " +
//								"manually.");
//						} else {
//							System.out.println("💀 LocalTunnel exited with code: " + exitCode);
//						}
//
//					} catch (Exception e) {
//						System.out.println("❌ Error starting LocalTunnel: " + e.getMessage());
//						System.out.println(
//							"💡 Ensure Node.js and LocalTunnel are installed. Alternatively, expose port 8080 " +
//							"manually" +
//							".");
//					}
//				});
//			}
//		};
//	}
}
