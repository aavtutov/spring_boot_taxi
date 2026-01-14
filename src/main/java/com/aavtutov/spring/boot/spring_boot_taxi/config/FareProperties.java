package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fare")
public class FareProperties {

	/** Base fare applied to every order. */
	private double base;

	/** Rate charged per kilometer traveled. */
	private double perKm;

	/** Rate charged per minute of the trip. */
	private double perMin;
	
	/** Currency code or symbol (e.g., $, USD, EUR). */
	private String currency;
}
