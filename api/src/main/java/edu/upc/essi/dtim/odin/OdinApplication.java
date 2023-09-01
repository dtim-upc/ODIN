package edu.upc.essi.dtim.odin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:credentials.properties")
public class OdinApplication {
	private static final Logger logger = LoggerFactory.getLogger(OdinApplication.class);

	public static void main(String[] args) {
		// Inicia la aplicación Spring Boot
		SpringApplication.run(OdinApplication.class, args);

		// Registra un mensaje en el registro de eventos cuando la aplicación se inicia correctamente
		logger.info("Application started. Ready to receive API requests.");
	}
}
