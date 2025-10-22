package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

@Component("distance_and_time")
public class FareCalculatorDistanceAndTime implements FareCalculator {

	private final FareProperties fareProperties;

	public FareCalculatorDistanceAndTime(FareProperties fareProperties) {
		this.fareProperties = fareProperties;
	}

	@Override
	public BigDecimal calculateFare(OrderEntity order) {

		double kms = order.getAproximateDistance().doubleValue();
		double minutes = order.getActualDuration().doubleValue();

		double baseFare = fareProperties.getBase();
		double pricePerKm = fareProperties.getPerKm();
		double pricePerMin = fareProperties.getPerMin();

		double totalPrice = baseFare + (kms * pricePerKm) + (minutes * pricePerMin);

		return BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
	}

}
