package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Service for managing the end-to-end taxi order lifecycle.
 * Handles placement, driver assignment, trip execution, and cancellations.
 */
public interface OrderService {

	/**
     * Creates a new order and initiates driver matching.
     */
	OrderEntity placeOrder(OrderEntity order, Long clientId);

	/**
     * Finds available drivers within proximity of the pickup location.
     */
	List<DriverEntity> findSuitableDrivers(Long orderId);

	/**
     * Links a driver to the order and sets status to ACCEPTED.
     */
	OrderEntity acceptOrder(Long orderId, Long telegramId);

	/**
     * Transitions the order to IN_PROGRESS (trip start).
     */
	OrderEntity startTrip(Long orderId, Long telegramId);

	/**
     * Finalizes the trip, calculates the final fare, and sets status to COMPLETED.
     */
	OrderEntity completeOrder(Long orderId, Long telegramId);

	OrderEntity cancelOrderByDriver(Long orderId, Long telegramId);

	OrderEntity cancelOrderByClient(Long orderId, Long telegramId);

	OrderEntity findOrderById(Long orderId);

	/**
     * Returns orders currently in PENDING status awaiting a driver.
     */
	List<OrderEntity> findAvailableOrders();

	Optional<OrderEntity> findActiveOrderByDriver(Long id);

	List<OrderEntity> findOrdersByClientId(Long clientId);

	/**
     * Returns the latest order created by the client.
     */
	OrderEntity findMostRecentOrderByClientId(Long clientId);

	List<OrderEntity> findOrdersByDriverId(Long driverId);
}
