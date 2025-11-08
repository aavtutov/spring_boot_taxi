package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Interface defining the contract for calculating the final price (fare) of a
 * taxi order.
 *
 * <p>
 * Implementations of this interface encapsulate the complex business logic of
 * pricing, which may involve distance, duration, time of day (surge pricing),
 * and fixed base rates.
 * </p>
 */
public interface FareCalculator {

	/**
	 * Calculates the total final fare for the given order based on its route,
	 * estimated distance, and any other relevant pricing factors.
	 *
	 * <p>
	 * The implementation should read relevant data from the {@link OrderEntity}
	 * (e.g., coordinates, approximate distance/duration) and return the calculated
	 * price.
	 * </p>
	 *
	 * @param order The {@link OrderEntity} containing all necessary data for fare
	 *              calculation.
	 * @return A {@link BigDecimal} representing the calculated base price of the
	 *         ride.
	 */
	BigDecimal calculateFare(OrderEntity order);

}
