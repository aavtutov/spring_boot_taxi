package com.aavtutov.spring.boot.spring_boot_taxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enables Spring's scheduled task execution capability (e.g., methods annotated
					// with @Scheduled).
public class SpringBootTaxiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTaxiApplication.class, args);
	}

}
