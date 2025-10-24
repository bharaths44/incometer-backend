package com.example.expensetracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner startLocalTunnel() {
		return args -> {
			System.out.println("🌐 CommandLineRunner running! Starting LocalTunnel...");

			Executors.newSingleThreadExecutor().submit(() -> {
				try {
					String os = System.getProperty("os.name").toLowerCase();
					String command = "npx lt --port 8080 --subdomain expensebot";

					ProcessBuilder pb = os.contains("win")
										? new ProcessBuilder("cmd.exe", "/c", command)
										: new ProcessBuilder("sh", "-c", command);

					// Ensure PATH is inherited so Node/NPM is found
					pb.environment().put("PATH", System.getenv("PATH"));
					pb.redirectErrorStream(true);

					System.out.println("📦 Executing: " + String.join(" ", pb.command()));

					Process process = pb.start();

					try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						String line;
						while ((line = reader.readLine()) != null) {
							System.out.println("🌍 " + line);
						}
					}

					int exitCode = process.waitFor();
					if (exitCode != 0) {
						System.out.println("❌ LocalTunnel failed. Trying ngrok as fallback...");
						startNgrok();
					} else {
						System.out.println("💀 LocalTunnel exited with code: " + exitCode);
					}

				} catch (Exception e) {
					System.out.println("❌ Error starting LocalTunnel: " + e.getMessage());
					System.out.println("🔄 Falling back to ngrok...");
					startNgrok();
				}
			});
		};
	}

	private void startNgrok() {
		try {
			String command = "npx ngrok http 8080";
			ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
			pb.environment().put("PATH", System.getenv("PATH"));
			pb.redirectErrorStream(true);

			System.out.println("📦 Executing: " + String.join(" ", pb.command()));

			Process process = pb.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("🌍 " + line);
				}
			}

			int exitCode = process.waitFor();
			System.out.println("💀 ngrok exited with code: " + exitCode);

		} catch (Exception e) {
			System.out.println("❌ Error starting ngrok: " + e.getMessage());
			System.out.println("💡 Ensure Node.js and ngrok are installed. Alternatively, expose port 8080 manually.");
		}
	}
}
