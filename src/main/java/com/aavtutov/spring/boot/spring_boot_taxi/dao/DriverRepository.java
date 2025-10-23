package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

/**
 * Spring Data JPA repository for managing {@link DriverEntity} data.
 *
 * <p>
 * Provides specialized lookup methods for drivers based on their unique
 * Telegram ID and their operational status.
 * </p>
 */
@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

	/**
	 * Finds a single driver entity by their unique Telegram ID.
	 *
	 * @param telegramId The unique ID assigned by Telegram to the user.
	 * @return An {@link Optional} containing the found driver, or empty if not
	 *         found.
	 */
	Optional<DriverEntity> findByTelegramId(Long telegramId);

	/**
	 * Finds a list of driver entities matching the specified operational status.
	 *
	 * <p>
	 * Used primarily to find available drivers (e.g., status: {@code ACTIVE}).
	 * </p>
	 *
	 * @param driverStatus The {@link DriverStatus} to filter by (e.g., ACTIVE,
	 *                     OFFLINE, PENDING_APPROVAL).
	 * @return A {@link List} of matching {@link DriverEntity} objects.
	 */
	List<DriverEntity> findByStatus(DriverStatus driverStatus);

}
