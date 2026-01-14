package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Strategy interface for calculating trip fares.
 * Implementations define pricing logic based on distance, duration, and other factors.
 */
public interface FareCalculator {

	/**
     * Calculates the total fare for the specified order.
     *
     * @param order The order entity containing route and timing details.
     * @return The calculated base price as a {@link BigDecimal}.
     */
	BigDecimal calculateFare(OrderEntity order);
}
