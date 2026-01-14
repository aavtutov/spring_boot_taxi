package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

import lombok.RequiredArgsConstructor;

@Component("DISTANCE_ONLY")
@RequiredArgsConstructor
public class FareCalculatorDistanceOnly implements FareCalculator {

	private final FareProperties fareProperties;

	@Override
	public BigDecimal calculateFare(OrderEntity order) {

		BigDecimal kms = order.getAproximateDistance();

		BigDecimal baseFare = BigDecimal.valueOf(fareProperties.getBase());
		BigDecimal pricePerKm = BigDecimal.valueOf(fareProperties.getPerKm());
		
		// Formula: Base + (Kms * RateKm)
		BigDecimal total = baseFare.add(kms.multiply(pricePerKm));

		return total.setScale(2, RoundingMode.HALF_UP);
	}
}
