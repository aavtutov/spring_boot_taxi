package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aavtutov.spring.boot.spring_boot_taxi.service.FareCalculator;

/**
 * Configuration for dynamic selection of the FareCalculator strategy.
 * The concrete implementation is picked at runtime based on the 'fare.calculation.strategy' property.
 */
@Configuration
public class FareCalculatorConfig {

	private final String strategy;
	
	public FareCalculatorConfig(@Value("${fare.calculation.strategy}") String strategy) {
        this.strategy = strategy;
    }
    
    @Bean
    FareCalculator fareCalculator(ApplicationContext context) {
    	// Strategy pattern: lookup the bean by name (e.g., DISTANCE_AND_TIME)
        return context.getBean(strategy.toUpperCase(), FareCalculator.class);
    }
}
