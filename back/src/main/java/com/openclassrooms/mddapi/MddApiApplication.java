package com.openclassrooms.mddapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the MDD (Monde de Dév) REST API.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MddApiApplication {

	/**
	 * Boots the Spring application context.
	 *
	 * @param args command-line arguments forwarded to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(MddApiApplication.class, args);
	}

}
