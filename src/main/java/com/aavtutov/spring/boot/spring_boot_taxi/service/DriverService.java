package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

/**
 * Service interface defining the business operations related to managing
 * {@link DriverEntity} data.
 *
 * <p>
 * This service handles driver registration, availability lookups, and
 * operational status management (e.g., setting a driver as ACTIVE, INACTIVE, or
 * BANNED).
 * </p>
 */
public interface DriverService {

	/**
	 * Registers a new driver in the system.
	 *
	 * <p>
	 * Upon registration, the driver's status is typically set to
	 * {@link DriverStatus#PENDING_APPROVAL}.
	 * </p>
	 *
	 * @param driver The new {@link DriverEntity} containing registration and
	 *               vehicle data.
	 * @return The newly created and persisted {@link DriverEntity} with generated
	 *         ID.
	 * @throws RuntimeException if a driver with the same unique identifier (e.g.,
	 *                          Telegram ID or license plate) already exists.
	 */
	DriverEntity registerDriver(DriverEntity driver);

	/**
	 * Finds and returns a list of drivers currently eligible to receive new orders.
	 *
	 * <p>
	 * Eligibility is typically based on the driver's status being
	 * {@link DriverStatus#ACTIVE}.
	 * </p>
	 *
	 * @return A list of available {@link DriverEntity} objects. Returns an empty
	 *         list if none are available.
	 */
	List<DriverEntity> findAvailableDrivers();

	/**
	 * Finds a driver by their mandatory Telegram ID.
	 *
	 * <p>
	 * This method is used when the driver must exist for the subsequent operation
	 * (e.g., order acceptance).
	 * </p>
	 *
	 * @param telegramId The unique Telegram ID of the driver.
	 * @return The found {@link DriverEntity}.
	 * @throws RuntimeException if the driver is not found.
	 */
	DriverEntity findDriverByTelegramId(Long telegramId);

	/**
	 * Finds a driver by their mandatory Telegram ID, returning the result wrapped
	 * in an {@link Optional}.
	 *
	 * <p>
	 * This method is preferred for checks where a driver may or may not exist
	 * (e.g., initial registration check).
	 * </p>
	 *
	 * @param telegramId The unique Telegram ID of the driver.
	 * @return An {@link Optional<DriverEntity>} which may be empty if no driver is
	 *         found.
	 */
	Optional<DriverEntity> findOptionalDriverByTelegramId(Long telegramId);

	/**
	 * Finds a driver by their internal primary key (ID).
	 *
	 * @param driverId The internal database ID of the driver.
	 * @return The found {@link DriverEntity}.
	 * @throws RuntimeException if the driver is not found.
	 */
	DriverEntity findDriverById(Long driverId);

	/**
	 * Sets the driver's status to {@link DriverStatus#ACTIVE}.
	 *
	 * <p>
	 * This is typically called in response to a driver signaling their availability
	 * (e.g., through a periodic "heartbeat" mechanism or login). This operation
	 * usually requires the driver's administrative status to be approved.
	 * </p>
	 *
	 * @param telegramId The Telegram ID of the driver to activate.
	 * @throws RuntimeException if the driver is not found or is not
	 *                          administratively approved.
	 */
	void activateDriverByHeartbeat(Long telegramId);

	/**
	 * Sets the driver's operational status to {@link DriverStatus#INACTIVE}.
	 *
	 * <p>
	 * This is typically called when the driver explicitly logs out or signals they
	 * are off-duty.
	 * </p>
	 *
	 * @param telegramId The Telegram ID of the driver to deactivate.
	 * @throws RuntimeException if the driver is not found.
	 */
	void deactivateDriver(Long telegramId);

	/**
	 * Updates the driver's administrative status, performed only by an
	 * administrator.
	 *
	 * <p>
	 * This allows moving a driver to statuses like {@link DriverStatus#ACTIVE}
	 * (after initial approval) or {@link DriverStatus#BANNED}.
	 * </p>
	 *
	 * @param driverId  The internal database ID of the driver.
	 * @param newStatus The target {@link DriverStatus} (e.g., ACTIVE, BANNED).
	 * @throws RuntimeException if the driver is not found.
	 */
	void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus);

}
