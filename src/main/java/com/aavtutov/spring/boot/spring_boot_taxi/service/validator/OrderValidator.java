package com.aavtutov.spring.boot.spring_boot_taxi.service.validator;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.AccessDeniedException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderStatusConflictException;

/**
 * Component responsible for validating the business rules related to order
 * status transitions and user access control before executing an action.
 *
 * <p>
 * This centralizes validation logic, ensuring that orders can only move between
 * states according to the predefined finite state machine rules.
 * </p>
 */
@Component
public class OrderValidator {

	/**
	 * Checks if the order is in a status that permits cancellation (i.e., not
	 * already CANCELED or COMPLETED).
	 *
	 * @param order The {@link OrderEntity} to validate.
	 * @throws OrderStatusConflictException if the order status is CANCELED or
	 *                                      COMPLETED.
	 */
	public void throwIfOrderStatusNotCancellable(OrderEntity order) {

		// Business Rule: Cannot cancel if the ride is already finished or previously
		// cancelled.
		if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.COMPLETED) {
			throw new OrderStatusConflictException(
					"Order cannot be cancelled in its current status: " + order.getStatus());
		}
	}

	/**
	 * Checks if the order is in the required status for completion (must be
	 * IN_PROGRESS).
	 *
	 * @param order The {@link OrderEntity} to validate.
	 * @throws OrderStatusConflictException if the order status is not IN_PROGRESS.
	 */
	public void throwIfOrderStatusNotCompletable(OrderEntity order) {

		// Business Rule: Completion must follow a STARTED (IN_PROGRESS) trip.
		if (order.getStatus() != OrderStatus.IN_PROGRESS) {
			throw new OrderStatusConflictException(
					"Only IN_PROGRESS orders can be completed. Current status: " + order.getStatus());
		}
	}

	/**
	 * Checks if the order is in the required status for a driver to accept it (must
	 * be PENDING).
	 *
	 * @param order The {@link OrderEntity} to validate.
	 * @throws OrderStatusConflictException if the order status is not PENDING.
	 */
	public void throwIfOrderStatusNotAcceptable(OrderEntity order) {

		// Business Rule: Only PENDING orders can be accepted.
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new OrderStatusConflictException(
					"Only PENDING orders can be accepted. Current status: " + order.getStatus());
		}
	}

	/**
	 * Checks if the order is in the required status for a driver to start the trip
	 * (must be ACCEPTED).
	 *
	 * @param order The {@link OrderEntity} to validate.
	 * @throws OrderStatusConflictException if the order status is not ACCEPTED.
	 */
	public void throwIfOrderStatusNotStartable(OrderEntity order) {

		// Business Rule: Starting a trip must follow driver acceptance (ACCEPTED).
		if (order.getStatus() != OrderStatus.ACCEPTED) {
			throw new OrderStatusConflictException(
					"Only ACCEPTED orders can be started. Current status: " + order.getStatus());
		}
	}

	/**
	 * Checks if the driver attempting the action is the driver currently assigned
	 * to the order.
	 *
	 * @param order    The {@link OrderEntity} to validate.
	 * @param driverId The ID of the driver attempting the action.
	 * @throws AccessDeniedException if the driver is not assigned to the order or
	 *                               the order has no assigned driver.
	 */
	public void throwIfDriverNotAssignedToOrder(OrderEntity order, Long driverId) {

		// Access Control Rule: Only the assigned driver can perform ACCEPTED, STARTED,
		// or CANCELED actions.
		if (order.getDriver() == null || !order.getDriver().getId().equals(driverId)) {
			throw new AccessDeniedException("Driver with id=" + driverId + " is not assigned to this order.");
		}
	}

	/**
	 * Checks if the client attempting the action is the client who placed the
	 * order.
	 *
	 * @param order    The {@link OrderEntity} to validate.
	 * @param clientId The ID of the client attempting the action.
	 * @throws AccessDeniedException if the client is not the creator of the order.
	 */
	public void throwIfClientNotAssignedToOrder(OrderEntity order, Long clientId) {

		// Access Control Rule: Only the client who placed the order can cancel or view
		// it.
		if (order.getClient() == null || !order.getClient().getId().equals(clientId)) {
			throw new AccessDeniedException("Client with id=" + clientId + " is not assigned to this order.");
		}
	}

}
