package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverNotFoundException;

@Service
public class DriverServiceImpl implements DriverService {

	private final DriverRepository driverRepository;

	public DriverServiceImpl(DriverRepository driverRepository) {
		this.driverRepository = driverRepository;
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	public DriverEntity findDriverByTelegramIdOrThrow(Long telegramId) {
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
	public DriverEntity setDriverOnlineStatus(Long driverId, Boolean isOnline) {
		DriverEntity driver = findDriverByIdOrThrow(driverId);
		driver.setIsOnline(isOnline);
		return driverRepository.save(driver);
	}

	@Override
	public List<DriverEntity> findAvailableDrivers() {
		return driverRepository.findByIsOnlineTrue();
	}

	@Override
	public DriverEntity findDriverByTelegramId(Long telegramId) {
		return findDriverByTelegramIdOrThrow(telegramId);
	}

	@Override
	public DriverEntity findDriverById(Long driverId) {
		return findDriverByIdOrThrow(driverId);
	}

}
