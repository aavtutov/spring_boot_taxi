package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Concrete implementation of the {@link FareCalculator} that calculates the
 * fare based on a fixed base fee plus charges for distance traveled and time
 * elapsed.
 *
 * <p>
 * This calculator is identified by the Spring component name
 * "distance_and_time" and relies on pricing parameters defined in
 * {@link FareProperties}.
 * </p>
 */
@Component("distance_and_time")
public class FareCalculatorDistanceAndTime implements FareCalculator {

	private final FareProperties fareProperties;

	/**
	 * Constructs the calculator, injecting the configuration properties containing
	 * the base, per-kilometer, and per-minute rates.
	 *
	 * @param fareProperties The configuration object holding the pricing structure.
	 */
	public FareCalculatorDistanceAndTime(FareProperties fareProperties) {
		this.fareProperties = fareProperties;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Calculates the total fare using the formula:
	 *             </p>
	 *             \[ \text{Total Price} = \text{Base Fare} + (\text{Distance (km)}
	 *             \times \text{Rate/km}) + (\text{Duration (min)} \times
	 *             \text{Rate/min}) \]
	 *
	 *             <p>
	 *             The result is rounded to two decimal places using
	 *             {@link RoundingMode#HALF_UP}.
	 *             </p>
	 *
	 * @param order The {@link OrderEntity} containing the
	 *              {@code aproximateDistance} and {@code actualDuration}.
	 * @return The final calculated price, rounded to two decimal places.
	 */
	@Override
	public BigDecimal calculateFare(OrderEntity order) {

		// Rationale: Converting BigDecimal to primitive double for simpler arithmetic
		// calculation,
		// which is acceptable for simple addition/multiplication before final rounding.
		double kms = order.getAproximateDistance().doubleValue();

		// Rationale: Using actualDuration for the time component. This might be zero if
		// calculation
		// happens before the trip starts, but should use estimated duration in that
		// case.
		// Based on the code, it uses whatever value is present in actualDuration.
		double minutes = order.getActualDuration().doubleValue();

		double baseFare = fareProperties.getBase();
		double pricePerKm = fareProperties.getPerKm();
		double pricePerMin = fareProperties.getPerMin();

		double totalPrice = baseFare + (kms * pricePerKm) + (minutes * pricePerMin);

		// Rationale: Rounding the final calculation to the standard monetary scale (2
		// decimal places)
		// using the common banking rounding mode (HALF_UP).
		return BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
	}

}
