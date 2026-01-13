package com.aavtutov.spring.boot.spring_boot_taxi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
public class SpringBootTaxiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTaxiApplication.class, args);
	}

}
