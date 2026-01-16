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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

	private final DriverRepository driverRepository;
	private final TaskScheduler taskScheduler;
	private final TelegramBotService telegramBotService;

	/**
	 * Time window after the last heartbeat before the driver is considered offline.
	 */
	private static final long DEACTIVATION_DELAY_MS = 60_000;
	private final Map<Long, ScheduledFuture<?>> scheduledDeactivations = new ConcurrentHashMap<>();

	@Override
	public DriverEntity registerDriver(DriverEntity driver) {
		driverRepository.findByTelegramId(driver.getTelegramId()).ifPresent(d -> {
			throw new DriverAlreadyExistsException("Driver already exists: " + d.getTelegramId());
		});
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
	public void activateDriverByHeartbeat(DriverEntity driver) {

		if (driver.getStatus() == DriverStatus.BANNED || driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
			return;
		}

		if (driver.getStatus() != DriverStatus.ACTIVE) {
			driver.setStatus(DriverStatus.ACTIVE);
			driverRepository.save(driver);
		}
		
		Long telegramId = driver.getTelegramId();

		Optional.ofNullable(scheduledDeactivations.remove(telegramId))
		.ifPresent(future -> future.cancel(false));
		
		scheduleDeactivation(telegramId);
	}	
	
	@Override
	public void deactivateDriver(Long telegramId) {
		driverRepository.findByTelegramId(telegramId).filter(driver -> driver.getStatus() == DriverStatus.ACTIVE)
				.ifPresent(driver -> {
					driver.setStatus(DriverStatus.INACTIVE);
					driverRepository.save(driver);
				});

		scheduledDeactivations.remove(telegramId);
	}
	
	@Override
	public void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus) {
		DriverEntity driver = findDriverByIdOrThrow(driverId);

		if (List.of(DriverStatus.BANNED, DriverStatus.PENDING_APPROVAL, DriverStatus.INACTIVE).contains(newStatus)) {
			Optional.ofNullable(scheduledDeactivations.remove(driver.getTelegramId()))
			.ifPresent(future -> future.cancel(false));
		}

		driver.setStatus(newStatus);
		driverRepository.save(driver);
		
		notifyDriver(driver);
	}
	
	@Override
	public Optional<DriverEntity> findByTelegramId(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId);
	}	
	
	// Private Helpers
	
	private void scheduleDeactivation(Long telegramId) {
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			deactivateDriver(telegramId);
		}, Instant.now().plusMillis(DEACTIVATION_DELAY_MS));

		scheduledDeactivations.put(telegramId, future);
	}
	
	private void notifyDriver(DriverEntity driver) {
        String message = switch (driver.getStatus()) {
            case BANNED -> "Your driver account has been banned!";
            case ACTIVE, INACTIVE -> "🎉 Your driver account is active now!";
            case PENDING_APPROVAL -> "Your driver account status has been changed to: PENDING";
            default -> throw new IllegalStateException("Unexpected status: " + driver.getStatus());
        };
        telegramBotService.sendMessage(driver.getTelegramChatId(), message);
    }
	
	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));
	}

	private DriverEntity findDriverByTelegramIdOrThrow(Long telegramId) {
		return driverRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new DriverNotFoundException("Driver not found: " + telegramId));
	}
}
