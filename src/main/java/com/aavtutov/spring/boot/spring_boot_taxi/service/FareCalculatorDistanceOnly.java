package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

@Component("distance_only")
public class FareCalculatorDistanceOnly implements FareCalculator {

	private final FareProperties fareProperties;

	public FareCalculatorDistanceOnly(FareProperties fareProperties) {
		this.fareProperties = fareProperties;
	}

	@Override
	public BigDecimal calculateFare(OrderEntity order) {

		double kms = order.getAproximateDistance().doubleValue();

		double baseFare = fareProperties.getBase();
		double pricePerKm = fareProperties.getPerKm();

		double totalPrice = baseFare + (kms * pricePerKm);

		return BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
	}

}
