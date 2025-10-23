package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

/**
 * Spring Data JPA repository for managing {@link OrderEntity} data.
 *
 * <p>
 * Provides specialized lookup methods covering the entire order lifecycle,
 * including client history, driver assignments, and active status checks.
 * </p>
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	/**
	 * Finds all orders that currently match the specified status.
	 *
	 * @param status The {@link OrderStatus} to filter by (e.g., PENDING, ACCEPTED).
	 * @return A list of matching orders.
	 */
	List<OrderEntity> findAllByStatus(OrderStatus status);

	/**
	 * Finds orders assigned to a specific driver whose status is within the
	 * provided list.
	 *
	 * @param driverId The ID of the driver.
	 * @param statuses A list of {@link OrderStatus} values to include in the
	 *                 search.
	 * @return A list of orders matching the criteria.
	 */
	List<OrderEntity> findByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

	/**
	 * Retrieves all orders placed by a specific client, sorted by creation date in
	 * descending order.
	 *
	 * <p>
	 * Used for displaying the client's historical list of orders.
	 * </p>
	 *
	 * @param clientId The ID of the client.
	 * @return A list of {@link OrderEntity}, from newest to oldest.
	 */
	List<OrderEntity> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

	/**
	 * Finds the single most recently created order by a specific client.
	 *
	 * <p>
	 * Used primarily to determine the last placed order, regardless of its status.
	 * </p>
	 *
	 * @param clientId The ID of the client.
	 * @return An {@link Optional} containing the latest order, or empty if no
	 *         orders exist.
	 */
	Optional<OrderEntity> findTopByClientIdOrderByCreatedAtDesc(Long clientId);

	/**
	 * Finds the first (most recently created) order assigned to a specific driver
	 * that matches one of the given statuses.
	 *
	 * <p>
	 * Typically used to find the driver's current active order (e.g., ACCEPTED,
	 * IN_PROGRESS).
	 * </p>
	 *
	 * @param driverId The ID of the driver.
	 * @param statuses A list of {@link OrderStatus} to check against.
	 * @return An {@link Optional} containing the active order, or empty if none is
	 *         found.
	 */
	Optional<OrderEntity> findFirstByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

	/**
	 * Checks if any order exists for the given client with a status in the provided
	 * list.
	 *
	 * <p>
	 * Used for pre-check/validation (e.g., checking if a client already has an
	 * active order).
	 * </p>
	 *
	 * @param clientId The ID of the client.
	 * @param statuses A list of {@link OrderStatus} to check.
	 * @return true if at least one matching order exists, false otherwise.
	 */
	boolean existsByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses);

	/**
	 * Checks if any order exists for the given driver with a status in the provided
	 * list.
	 *
	 * <p>
	 * Used for pre-check/validation (e.g., checking if a driver is currently busy
	 * with an active order).
	 * </p>
	 *
	 * @param driverId The ID of the driver.
	 * @param statuses A list of {@link OrderStatus} to check.
	 * @return true if at least one matching order exists, false otherwise.
	 */
	boolean existsByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

}