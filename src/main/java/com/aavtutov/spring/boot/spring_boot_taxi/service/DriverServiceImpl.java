package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverNotFoundException;

import jakarta.transaction.Transactional;

/**
 * Default implementation of the {@link DriverService} interface.
 *
 * <p>
 * Handles core business logic related to driver management, including
 * registration, status checks, and the critical operational logic for driver
 * availability via scheduled 'heartbeat' mechanism.
 * </p>
 */
@Service
public class DriverServiceImpl implements DriverService {

	private final DriverRepository driverRepository;
	private final TaskScheduler taskScheduler;
	private final TelegramBotService telegramBotService;

	/**
	 * The delay (in milliseconds) after the last heartbeat before a driver is
	 * automatically switched from ACTIVE to INACTIVE status. (e.g., 60000ms = 1
	 * minute).
	 */
	private static final long DEACTIVATION_DELAY_MS = 60000;

	/**
	 * Map to store scheduled deactivation tasks. Key is the driver's Telegram ID,
	 * value is the {@link ScheduledFuture} representing the pending deactivation
	 * job. This is used to cancel the previous task upon a new heartbeat.
	 */
	private final Map<Long, ScheduledFuture<?>> scheduledDeactivations = new ConcurrentHashMap<>();

	/**
	 * Constructs the service, injecting required dependencies.
	 *
	 * @param driverRepository   The DAO for interacting with the driver persistence
	 *                           layer.
	 * @param taskScheduler      The Spring scheduler used for managing automatic
	 *                           deactivation tasks (heartbeat).
	 * @param telegramBotService Service for sending status updates/notifications to
	 *                           the driver via Telegram.
	 */
	public DriverServiceImpl(DriverRepository driverRepository, TaskScheduler taskScheduler,
			TelegramBotService telegramBotService) {
		this.driverRepository = driverRepository;
		this.taskScheduler = taskScheduler;
		this.telegramBotService = telegramBotService;
	}

	// --- Private Utility Methods ---

	/**
	 * Finds a driver by internal database ID or throws an exception.
	 */
	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	/**
	 * Finds a driver by Telegram ID or throws an exception.
	 */
	private DriverEntity findDriverByTelegramIdOrThrow(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with telegram_id=" + telegramId + " not found"));
	}

	// --- Service Implementation Methods ---

	/**
	 * @inheritDoc
	 *             <p>
	 *             Registers a new driver, ensuring no existing driver has the same
	 *             Telegram ID. The status defaults to
	 *             {@link DriverStatus#PENDING_APPROVAL} in the entity.
	 *             </p>
	 *
	 * @throws DriverAlreadyExistsException if a driver with the given Telegram ID
	 *                                      already exists.
	 */
	@Override
	public DriverEntity registerDriver(DriverEntity driver) {
		// Business Rule: Check for duplicate registration.
		if (driverRepository.findByTelegramId(driver.getTelegramId()).isPresent()) {
			throw new DriverAlreadyExistsException(
					"Driver with telegramId " + driver.getTelegramId() + " already exists");
		}
		return driverRepository.save(driver);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public DriverEntity findDriverByTelegramId(Long telegramId) {
		return findDriverByTelegramIdOrThrow(telegramId);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public DriverEntity findDriverById(Long driverId) {
		return findDriverByIdOrThrow(driverId);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<DriverEntity> findAvailableDrivers() {
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Processes a driver's 'heartbeat' signal. This method ensures the
	 *             driver is ACTIVE (if eligible) and resets the automatic
	 *             deactivation timer.
	 *             </p>
	 */
	@Transactional
	@Override
	public void activateDriverByHeartbeat(Long telegramId) {
		DriverEntity driver = findDriverByTelegramIdOrThrow(telegramId);

		// Security/Business Rule: Ignore heartbeats if the driver is BANNED or not yet
		// approved.
		if (driver.getStatus() == DriverStatus.BANNED || driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
			return;
		}

		// 1. Update status to ACTIVE if it was INACTIVE (e.g., driver just logged in or
		// came back online).
		if (driver.getStatus() != DriverStatus.ACTIVE) {
			driver.setStatus(DriverStatus.ACTIVE);
			driverRepository.save(driver);
		}

		// 2. Cancel the previous scheduled deactivation task (reset the timer).
		ScheduledFuture<?> future = scheduledDeactivations.remove(telegramId);
		if (future != null) {
			future.cancel(false);
		}

		// 3. Schedule a new deactivation task.
		scheduleDeactivation(telegramId);
	}

	/**
	 * Schedules a task to automatically switch the driver to INACTIVE status after
	 * a defined delay if no subsequent heartbeat signal is received.
	 *
	 * @param telegramId The ID of the driver for whom to schedule deactivation.
	 */
	private void scheduleDeactivation(Long telegramId) {
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			// Execute the deactivation logic
			deactivateDriver(telegramId);
		}, Instant.now().plusMillis(DEACTIVATION_DELAY_MS));

		// Store the future to allow cancellation upon next heartbeat
		scheduledDeactivations.put(telegramId, future);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Sets the driver's status to {@link DriverStatus#INACTIVE} if they
	 *             were previously ACTIVE. This method is called either explicitly
	 *             (driver logout) or automatically (heartbeat timeout).
	 *             </p>
	 */
	@Override
	public void deactivateDriver(Long telegramId) {
		// Rationale: Use findByTelegramId().orElse(null) because this is often called
		// asynchronously
		// and we cannot guarantee the driver still exists or the calling thread can
		// handle an exception.
		DriverEntity driver = driverRepository.findByTelegramId(telegramId).orElse(null);

		// Only update status if the driver exists and is currently ACTIVE.
		if (driver != null && driver.getStatus() == DriverStatus.ACTIVE) {
			driver.setStatus(DriverStatus.INACTIVE);
			driverRepository.save(driver);
		}

		// Remove the future from the map regardless of whether the update occurred.
		scheduledDeactivations.remove(telegramId);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Performs an administrative status update, allowing manual changes
	 *             to the driver's eligibility (e.g., BANNED, PENDING_APPROVAL).
	 *             Also handles cancellation of pending deactivation timers.
	 *             </p>
	 */
	@Override
	public void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus) {

		DriverEntity driver = findDriverByIdOrThrow(driverId);

		// Logic: If the new status makes the driver ineligible for active orders,
		// cancel any pending deactivation timer.
		if (newStatus == DriverStatus.BANNED || newStatus == DriverStatus.PENDING_APPROVAL
				|| newStatus == DriverStatus.INACTIVE) {

			ScheduledFuture<?> future = scheduledDeactivations.remove(driver.getTelegramId());
			if (future != null) {
				future.cancel(false);
			}
		}

		driver.setStatus(newStatus);
		driverRepository.save(driver);

		String chatId = driver.getTelegramChatId();

		// Notify the driver about the administrative status change.
		switch (driver.getStatus()) {
		case BANNED -> telegramBotService.sendMessage(chatId, "Your driver account has been banned");
		case PENDING_APPROVAL ->
			telegramBotService.sendMessage(chatId, "Your driver account status has been changed to PENDING");
		default -> throw new IllegalStateException("Unexpected driver status");

		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<DriverEntity> findOptionalDriverByTelegramId(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId);
	}

}
