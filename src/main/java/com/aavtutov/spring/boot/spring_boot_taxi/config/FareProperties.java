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

	private double base;

	private double perKm;

	private double perMin;
}
