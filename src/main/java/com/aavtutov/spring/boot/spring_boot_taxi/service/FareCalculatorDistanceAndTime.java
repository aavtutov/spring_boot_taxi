package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FareCalculatorDistanceAndTime implements FareCalculator {

	private final FareProperties fareProperties;

	@Override
	public BigDecimal calculateFare(OrderEntity order) {
		
		BigDecimal kms = order.getAproximateDistance();
		BigDecimal minutes = order.getActualDuration();
		
		BigDecimal baseFare = BigDecimal.valueOf(fareProperties.getBase());
        BigDecimal pricePerKm = BigDecimal.valueOf(fareProperties.getPerKm());
        BigDecimal pricePerMin = BigDecimal.valueOf(fareProperties.getPerMin());

        // Formula: Base + (Kms * RateKm) + (Minutes * RateMin)
        BigDecimal total = baseFare
        		.add(kms.multiply(pricePerKm))
        		.add(minutes.multiply(pricePerMin));
        		
		return total.setScale(2, RoundingMode.HALF_UP);
	}
}
