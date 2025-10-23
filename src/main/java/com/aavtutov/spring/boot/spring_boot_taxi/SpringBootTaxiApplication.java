package com.aavtutov.spring.boot.spring_boot_taxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for the Spring Boot Taxi application.
 *
 * <p>This class bootstraps and launches the application context.</p>
 */
@SpringBootApplication
@EnableScheduling // Enables Spring's scheduled task execution capability (e.g., methods annotated with @Scheduled).
public class SpringBootTaxiApplication {

	/**
     * The main method that uses Spring Boot's static run method to start the application.
     *
     * @param args Command line arguments passed to the application.
     */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootTaxiApplication.class, args);
	}

}
