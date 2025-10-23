package com.aavtutov.spring.boot.spring_boot_taxi.dto;

/**
 * Enumeration defining all possible actions that can be taken to modify the
 * state of a taxi order.
 *
 * <p>
 * These actions are used in the {@code OrderUpdateDTO} to signal state
 * transitions.
 * </p>
 */
public enum OrderAction {

	/**
	 * Action initiated by a driver to accept a pending order.
	 */
	ACCEPT,

	/**
	 * Action initiated by a driver to signal that they have picked up the client
	 * and the trip has started.
	 */
	START_TRIP,

	/**
	 * Action initiated by a driver to signal that the trip is finished and the
	 * order is complete.
	 */
	COMPLETE,

	/**
	 * Action initiated by a driver to cancel an order (e.g., due to no-show or
	 * issue).
	 */
	CANCEL_BY_DRIVER,

	/**
	 * Action initiated by a client to cancel an order.
	 */
	CANCEL_BY_CLIENT;

	/**
	 * Checks if the current action is one that must be initiated by an
	 * authenticated driver.
	 *
	 * <p>
	 * Driver actions include ACCEPT, START_TRIP, COMPLETE, and CANCEL_BY_DRIVER.
	 * </p>
	 *
	 * @return true if the action requires driver authorization, false otherwise.
	 */
	public boolean isDriverAction() {
		return this == ACCEPT || this == START_TRIP || this == COMPLETE || this == CANCEL_BY_DRIVER;
	}
}
