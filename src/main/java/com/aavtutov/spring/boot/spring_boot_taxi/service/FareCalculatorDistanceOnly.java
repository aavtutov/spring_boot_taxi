package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Concrete implementation of the {@link FareCalculator} that calculates the
 * fare strictly based on a fixed base fee plus charges for the estimated
 * distance traveled. This model excludes time-based charges.
 *
 * <p>
 * This calculator is identified by the Spring component name "distance_only"
 * and relies on pricing parameters defined in {@link FareProperties}.
 * </p>
 */
@Component("distance_only")
public class FareCalculatorDistanceOnly implements FareCalculator {

	private final FareProperties fareProperties;

	/**
	 * Constructs the calculator, injecting the configuration properties containing
	 * the base and per-kilometer rates.
	 *
	 * @param fareProperties The configuration object holding the pricing structure.
	 */
	public FareCalculatorDistanceOnly(FareProperties fareProperties) {
		this.fareProperties = fareProperties;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Calculates the total fare using the simplified formula:
	 *             </p>
	 *             \[ \text{Total Price} = \text{Base Fare} + (\text{Distance (km)}
	 *             \times \text{Rate/km}) \]
	 *
	 *             <p>
	 *             The result is rounded to two decimal places using
	 *             {@link RoundingMode#HALF_UP}.
	 *             </p>
	 *
	 * @param order The {@link OrderEntity} containing the
	 *              {@code aproximateDistance}.
	 * @return The final calculated price, rounded to two decimal places.
	 */
	@Override
	public BigDecimal calculateFare(OrderEntity order) {

		// Rationale: Retrieves the estimated distance for calculation.
		double kms = order.getAproximateDistance().doubleValue();

		double baseFare = fareProperties.getBase();
		double pricePerKm = fareProperties.getPerKm();

		// Core business logic: Base + Distance Charge. Time charge is explicitly
		// excluded.
		double totalPrice = baseFare + (kms * pricePerKm);

		// Rationale: Rounding the final calculation to the standard monetary scale (2
		// decimal places).
		return BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
	}

}
