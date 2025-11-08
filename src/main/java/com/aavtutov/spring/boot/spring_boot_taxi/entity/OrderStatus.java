package com.aavtutov.spring.boot.spring_boot_taxi.entity;

/**
 * Enumeration defining the current status within the lifecycle of an
 * {@link OrderEntity}.
 *
 * <p>
 * This status controls the business rules applied to the order (e.g., who can
 * update it, whether a client can place a new order).
 * </p>
 */
public enum OrderStatus {

	/**
	 * The order has been placed by the client and is currently waiting for a driver
	 * to accept it.
	 */
	PENDING,

	/**
	 * A driver has accepted the order and is currently en route to the client's
	 * pickup location.
	 */
	ACCEPTED,

	/**
	 * The driver has picked up the client, and the trip is currently in transit to
	 * the destination.
	 */
	IN_PROGRESS,

	/**
	 * The trip is finished, and the order is successfully completed.
	 */
	COMPLETED,

	/**
	 * The order was canceled by either the client, the driver, or the system at any
	 * point before completion.
	 */
	CANCELED;

}
