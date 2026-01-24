package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aavtutov.spring.boot.spring_boot_taxi.service.FareCalculator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.FareCalculatorDistanceAndTime;
import com.aavtutov.spring.boot.spring_boot_taxi.service.FareCalculatorDistanceOnly;

/**
 * Configuration for dynamic selection of the FareCalculator strategy.
 * The concrete implementation is picked at application startup based on the 'fare.calculation.strategy' property.
 */
@Configuration
public class FareCalculatorConfig {
    
    @Bean
    @ConditionalOnProperty(name = "fare.calculation.strategy", havingValue = "DISTANCE_AND_TIME")
    FareCalculator distanceAndTime(FareProperties fareProperties) {
        return new FareCalculatorDistanceAndTime(fareProperties);
    }
    
    @Bean
    @ConditionalOnProperty(name = "fare.calculation.strategy", havingValue = "DISTANCE_ONLY")
    FareCalculator distanceOnly(FareProperties fareProperties) {
        return new FareCalculatorDistanceOnly(fareProperties);
    }
}
