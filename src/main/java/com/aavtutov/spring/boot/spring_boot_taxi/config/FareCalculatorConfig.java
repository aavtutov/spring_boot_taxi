package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aavtutov.spring.boot.spring_boot_taxi.service.FareCalculator;

/**
 * Configuration class responsible for dynamically selecting the concrete {@link FareCalculator}
 * implementation based on a configuration property.
 *
 * <p>This enables a Strategy Pattern implementation for fare calculation.</p>
 */
@Configuration
public class FareCalculatorConfig {

	/**
     * The name of the desired {@link FareCalculator} bean to be used.
     * This value is read from the Spring environment (e.g., application.properties).
     */
    @Value("${fare.calculation.strategy}")
    private String strategy;
    
    /**
     * Creates and exposes the concrete {@link FareCalculator} bean to the application context.
     *
     * <p>The implementation is dynamically selected by retrieving a bean whose name
     * matches the configured {@code fare.calculation.strategy} value.</p>
     *
     * @param context The current Spring {@link ApplicationContext} used for dynamic bean lookup.
     * @return The specific {@link FareCalculator} implementation (e.g., BasicFareCalculator, PremiumFareCalculator).
     */
    @Bean
    FareCalculator fareCalculator(ApplicationContext context) {
    	// Rationale: Use the ApplicationContext to fetch the correct implementation 
        // by name (strategy) at runtime, implementing the Strategy pattern.
        return context.getBean(strategy.toUpperCase(), FareCalculator.class);
    }
}
