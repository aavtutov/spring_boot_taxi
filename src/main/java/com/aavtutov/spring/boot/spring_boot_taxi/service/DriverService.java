package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

/**
 * Service for managing driver lifecycle, availability, and administrative status.
 */
public interface DriverService {

	/**
     * Registers a new driver with PENDING_APPROVAL status.
     */
	DriverEntity registerDriver(DriverEntity driver);

	/**
     * Returns a list of drivers currently eligible to receive orders (ACTIVE status).
     */
	List<DriverEntity> findAvailableDrivers();

	DriverEntity findDriverByTelegramId(Long telegramId);

	Optional<DriverEntity> findOptionalDriverByTelegramId(Long telegramId);

	DriverEntity findDriverById(Long driverId);

	/**
     * Activates the driver and resets the auto-deactivation timer (heartbeat).
     */
	void activateDriverByHeartbeat(Long telegramId);

	/**
     * Manually sets the driver to INACTIVE (off-duty).
     */
	void deactivateDriver(Long telegramId);

	/**
     * Administrative update for driver status, including BANNED or APPROVED states.
     */
	void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus);
}
