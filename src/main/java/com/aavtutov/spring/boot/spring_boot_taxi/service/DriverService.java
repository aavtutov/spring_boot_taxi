package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

public interface DriverService {

	DriverEntity registerDriver(DriverEntity driver);

	List<DriverEntity> findAvailableDrivers();

	DriverEntity findDriverByTelegramId(Long telegramId);
	
	Optional<DriverEntity> findOptionalDriverByTelegramId(Long telegramId);

	DriverEntity findDriverById(Long driverId);

	void activateDriverByHeartbeat(Long telegramId);

	void deactivateDriver(Long telegramId);
	
	void adminUpdateDriverStatus(Long driverId, DriverStatus newStatus);

}
