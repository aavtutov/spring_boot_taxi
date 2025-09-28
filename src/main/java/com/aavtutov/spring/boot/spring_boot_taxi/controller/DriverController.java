package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

	private final DriverService driverService;
	private final DriverMapper driverMapper;

	public DriverController(DriverService driverService, DriverMapper driverMapper) {
		this.driverService = driverService;
		this.driverMapper = driverMapper;
	}

	private DriverResponseDTO toResponseDto(DriverEntity driverEntity) {
		return driverMapper.toResponseDto(driverEntity);
	}

	@PostMapping
	public DriverResponseDTO registerDriver(@RequestBody @Valid DriverCreateDTO driverDTO) {
		DriverEntity driver = driverMapper.fromCreateDto(driverDTO);
		DriverEntity registeredDriver = driverService.registerDriver(driver);
		return toResponseDto(registeredDriver);
	}

	@PatchMapping("/{id}")
	public DriverResponseDTO updateDriverOnlineStatus(@PathVariable("id") Long driverId,
			@RequestBody @Valid DriverUpdateDTO driverDTO) {
		DriverEntity updatedDriver = driverService.setDriverOnlineStatus(driverId, driverDTO.getIsOnline());
		return toResponseDto(updatedDriver);
	}

	@GetMapping
	public List<DriverResponseDTO> findAvailableDrivers() {
		List<DriverEntity> availableDrivers = driverService.findAvailableDrivers();
		return availableDrivers.stream().map(driver -> toResponseDto(driver)).toList();
	}

	@GetMapping("/by-telegram-id/{id}")
	public DriverResponseDTO findDriverByTelegramId(@PathVariable("id") Long telegramId) {
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
		return toResponseDto(driver);
	}

	@GetMapping("/by-id/{id}")
	public DriverResponseDTO findDriverById(@PathVariable("id") Long driverId) {
		DriverEntity driver = driverService.findDriverById(driverId);
		return toResponseDto(driver);
	}

}
