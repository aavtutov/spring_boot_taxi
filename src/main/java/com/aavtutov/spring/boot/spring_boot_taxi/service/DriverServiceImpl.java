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

@Service
public class DriverServiceImpl implements DriverService {

	private final DriverRepository driverRepository;
	private final TaskScheduler taskScheduler;
	private final TelegramBotService telegramBotService;

	private static final long DEACTIVATION_DELAY_MS = 60000;
	private final Map<Long, ScheduledFuture<?>> scheduledDeactivations = new ConcurrentHashMap<>();

	public DriverServiceImpl(DriverRepository driverRepository, TaskScheduler taskScheduler, TelegramBotService telegramBotService) {
		this.driverRepository = driverRepository;
		this.taskScheduler = taskScheduler;
		this.telegramBotService = telegramBotService;
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	private DriverEntity findDriverByTelegramIdOrThrow(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with telegram_id=" + telegramId + " not found"));
	}

	@Override
	public DriverEntity registerDriver(DriverEntity driver) {
		if (driverRepository.findByTelegramId(driver.getTelegramId()).isPresent()) {
			throw new DriverAlreadyExistsException(
					"Driver with telegramId " + driver.getTelegramId() + " already exists");
		}
		return driverRepository.save(driver);
	}

	@Override
	public DriverEntity findDriverByTelegramId(Long telegramId) {
		return findDriverByTelegramIdOrThrow(telegramId);
	}

	@Override
	public DriverEntity findDriverById(Long driverId) {
		return findDriverByIdOrThrow(driverId);
	}

	@Override
	public List<DriverEntity> findAvailableDrivers() {
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}

	@Transactional
	@Override
	public void activateDriverByHeartbeat(Long telegramId) {
		DriverEntity driver = findDriverByTelegramIdOrThrow(telegramId);

		if (driver.getStatus() == DriverStatus.BANNED || driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
			return;
		}

		if (driver.getStatus() != DriverStatus.ACTIVE) {
			driver.setStatus(DriverStatus.ACTIVE);
			driverRepository.save(driver);
		}

		ScheduledFuture<?> future = scheduledDeactivations.remove(telegramId);
		if (future != null) {
			future.cancel(false);
		}

		scheduleDeactivation(telegramId);
	}

	private void scheduleDeactivation(Long telegramId) {
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			deactivateDriver(telegramId);
		}, Instant.now().plusMillis(DEACTIVATION_DELAY_MS));
		scheduledDeactivations.put(telegramId, future);
	}

	@Override
	public void deactivateDriver(Long telegramId) {
		DriverEntity driver = driverRepository.findByTelegramId(telegramId).orElse(null);

		if (driver != null && driver.getStatus() == DriverStatus.ACTIVE) {
			driver.setStatus(DriverStatus.INACTIVE);
			driverRepository.save(driver);
		}
		scheduledDeactivations.remove(telegramId);
	}

	@Override
	public void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus) {

		DriverEntity driver = findDriverByIdOrThrow(driverId);

		// 2. Heartbeat Cancellation Logic (for Security)
		// If the status changes to BANNED (PENDING_APPROVAL) or simply INACTIVE
		// we should cancel the scheduled automatic shutdown.
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
		
		switch(driver.getStatus()) {
		
		case INACTIVE -> telegramBotService.sendMessage(chatId, "Your driver account has been activated");
		case BANNED -> telegramBotService.sendMessage(chatId, "Your driver account has been banned");
		case PENDING_APPROVAL -> telegramBotService.sendMessage(chatId, "Your driver account status has been changed to PENDING");
		default -> throw new IllegalStateException("Unexpected driver status");
		
		}
		
		// TODO: implement admin web client
	}

	@Override
	public Optional<DriverEntity> findOptionalDriverByTelegramId(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId);
	}

}
