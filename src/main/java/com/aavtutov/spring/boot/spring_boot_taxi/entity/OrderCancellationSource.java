package com.aavtutov.spring.boot.spring_boot_taxi.entity;

/**
 * Enumeration defining the source or party responsible for initiating the
 * cancellation of an {@link OrderEntity}.
 *
 * <p>Used to track accountability and apply appropriate business rules (e.g., penalties).</p>
 */
public enum OrderCancellationSource {

	/**
     * The order was cancelled by the client (passenger).
     */
	CLIENT,
	
	/**
     * The order was cancelled by the assigned driver.
     */
	DRIVER,
	
	/**
     * The order was cancelled automatically by the system (e.g., due to timeout
     * while searching for a driver, or internal error).
     */
	SYSTEM;

}
