package pl.edu.mimuw.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class ClientApplication {
	public static void main(String[] args) {
		if (args.length > 0) {
			SignerHostnameSingleton.signerHostname = args[0].trim();
		}
		System.out.println("Using Signer hostname: " + SignerHostnameSingleton.signerHostname);
		SpringApplication.run(ClientApplication.class, args);
	}
}
