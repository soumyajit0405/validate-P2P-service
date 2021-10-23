package com.energytrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
/**
 * 
 * @author Soumyajit
 *
 */
@SpringBootApplication
@EnableJpaAuditing
public class ValidateServiceApplication {

	public static void main(String[] args) {
		System.out.println("Starting Validate Service Application");
		//System.setProperty("server.servlet.context-path");
		SpringApplication.run(ValidateServiceApplication.class, args);
		//, "/inmo"
		
	}
}
