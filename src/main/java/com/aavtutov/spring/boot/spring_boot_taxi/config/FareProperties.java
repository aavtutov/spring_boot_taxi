package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class that holds properties related to taxi fare calculation.
 *
 * <p>
 * These values are mapped from the Spring environment under the prefix "fare".
 * </p>
 * <p>
 * Example: {@code fare.base} maps to the {@code base} field.
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fare")
public class FareProperties {

	/**
	 * Configuration class that holds properties related to taxi fare calculation.
	 *
	 * <p>
	 * These values are mapped from the Spring environment under the prefix "fare".
	 * </p>
	 * <p>
	 * Example: {@code fare.base} maps to the {@code base} field.
	 * </p>
	 */
	private double base;

	/**
	 * The cost per kilometer travelled. Mapped from {@code fare.perKm}.
	 */
	private double perKm;

	/**
	 * The cost per minute of the ride duration. Mapped from {@code fare.perMin}.
	 */
	private double perMin;
}
