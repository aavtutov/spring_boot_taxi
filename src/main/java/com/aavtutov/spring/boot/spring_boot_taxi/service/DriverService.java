package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

public interface DriverService {

	DriverEntity registerDriver(DriverEntity driver);

	DriverEntity setDriverOnlineStatus(Long driverId, Boolean isOnline);

	List<DriverEntity> findAvailableDrivers();

	DriverEntity findDriverByTelegramId(Long telegramId);

	DriverEntity findDriverById(Long driverId);

}
