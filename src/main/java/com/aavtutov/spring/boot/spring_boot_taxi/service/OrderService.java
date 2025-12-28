package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Service interface defining the core business operations for managing the
 * lifecycle of taxi orders.
 *
 * <p>
 * This service orchestrates order placement, driver matching, status updates
 * (acceptance, start, completion), and cancellation scenarios for both clients
 * and drivers.
 * </p>
 */
public interface OrderService {

	/**
	 * Creates a new taxi order and initiates the process of finding a suitable
	 * driver.
	 *
	 * <p>
	 * This operation typically involves calculating the initial fare and saving the
	 * order with a PENDING status.
	 * </p>
	 *
	 * @param order    The {@link OrderEntity} containing trip details (start/end
	 *                 coordinates).
	 * @param clientId The ID of the client placing the order.
	 * @return The newly created and persisted {@link OrderEntity}.
	 * @throws RuntimeException if the client is not found or if there is a
	 *                          validation issue.
	 */
	OrderEntity placeOrder(OrderEntity order, Long clientId);

	/**
	 * Identifies and returns a list of drivers who are currently available and
	 * geographically suitable to take the specified order.
	 *
	 * <p>
	 * Suitability is determined by factors like driver status and proximity to the
	 * pickup location.
	 * </p>
	 *
	 * @param orderId The ID of the order requiring a driver.
	 * @return A list of potentially suitable {@link DriverEntity} objects.
	 * @throws RuntimeException if the order is not found.
	 */
	List<DriverEntity> findSuitableDrivers(Long orderId);

	/**
	 * Confirms that a specific driver has accepted the order.
	 *
	 * <p>
	 * This updates the order status to ACCEPTED and links the driver to the order.
	 * </p>
	 *
	 * @param orderId  The ID of the order being accepted.
	 * @param driverId The ID of the driver accepting the order.
	 * @return The updated {@link OrderEntity} with the driver assigned.
	 * @throws RuntimeException if the order or driver is not found, or if the order
	 *                          is no longer in a PENDING state.
	 */
	OrderEntity acceptOrder(Long orderId, Long driverId);

	/**
	 * Marks the trip as started, typically when the driver has arrived to the
	 * client.
	 *
	 * <p>
	 * The order status is updated to IN_PROGRESS.
	 * </p>
	 *
	 * @param orderId  The ID of the order to start.
	 * @param driverId The ID of the driver initiating the start.
	 * @return The updated {@link OrderEntity}.
	 * @throws RuntimeException if the order is not in the ACCEPTED state.
	 */
	OrderEntity startTrip(Long orderId, Long driverId);

	/**
	 * Marks the trip as completed, typically when the driver reaches the
	 * destination.
	 *
	 * <p>
	 * This operation should finalize the order details, calculate the final fare,
	 * and update the status to COMPLETED.
	 * </p>
	 *
	 * @param orderId  The ID of the order to complete.
	 * @param driverId The ID of the driver completing the order.
	 * @return The final {@link OrderEntity} including the calculated fare.
	 * @throws RuntimeException if the order is not in the IN_PROGRESS state.
	 */
	OrderEntity completeOrder(Long orderId, Long driverId);

	/**
	 * Cancels an order initiated by the assigned driver.
	 *
	 * <p>
	 * The order status is updated to CANCELLED_BY_DRIVER. Specific business rules
	 * may apply depending on the current order status (e.g., penalties).
	 * </p>
	 *
	 * @param orderId  The ID of the order to cancel.
	 * @param driverId The ID of the driver cancelling the order.
	 * @return The updated {@link OrderEntity}.
	 * @throws RuntimeException if the order or driver is not found, or if the order
	 *                          is already COMPLETED.
	 */
	OrderEntity cancelOrderByDriver(Long orderId, Long driverId);

	/**
	 * Cancels an order initiated by the client.
	 *
	 * <p>
	 * The order status is updated to CANCELLED_BY_CLIENT. Specific business rules
	 * may apply depending on the current order status (e.g., cancellation fee if
	 * the driver has already accepted).
	 * </p>
	 *
	 * @param orderId  The ID of the order to cancel.
	 * @param clientId The ID of the client cancelling the order.
	 * @return The updated {@link OrderEntity}.
	 * @throws RuntimeException if the order or client is not found, or if the order
	 *                          is already COMPLETED.
	 */
	OrderEntity cancelOrderByClient(Long orderId, Long clientId);

	/**
	 * Retrieves an order by its internal primary key ID.
	 *
	 * @param orderId The internal database ID of the order.
	 * @return The found {@link OrderEntity}.
	 * @throws RuntimeException if the order is not found.
	 */
	OrderEntity findOrderById(Long orderId);

	/**
	 * Retrieves a list of orders that are currently waiting for driver assignment.
	 *
	 * <p>
	 * These are orders typically in the PENDING status.
	 * </p>
	 *
	 * @return A list of available {@link OrderEntity} objects.
	 */
	List<OrderEntity> findAvailableOrders();

	/**
	 * Retrieves the single currently active (IN_PROGRESS or ACCEPTED) order
	 * associated with a driver.
	 *
	 * @param driverId The ID of the driver.
	 * @return An {@link Optional<OrderEntity>} containing the active order, or
	 *         empty if none is active.
	 */
	Optional<OrderEntity> findActiveOrderByDriver(Long id);

	/**
	 * Retrieves a list of all historical orders placed by a specific client.
	 *
	 * @param clientId The ID of the client.
	 * @return A list of {@link OrderEntity} objects associated with the client.
	 */
	List<OrderEntity> findOrdersByClientId(Long clientId);

	/**
	 * Retrieves the client's single most recently created order, regardless of its
	 * status. This order is typically used by the client UI to determine the last
	 * action or current status.
	 *
	 * @param clientId The ID of the client.
	 * @return The most recent {@link OrderEntity} placed by the client.
	 * @throws NoContentException if the client has placed no orders at all.
	 */
	OrderEntity findMostRecentOrderByClientId(Long clientId);

	/**
	 * Retrieves a list of all historical and current orders assigned to a specific
	 * driver.
	 *
	 * @param driverId The ID of the driver.
	 * @return A list of {@link OrderEntity} objects associated with the driver.
	 */
	List<OrderEntity> findOrdersByDriverId(Long driverId);

}
