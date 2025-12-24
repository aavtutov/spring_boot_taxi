package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/drivers")
public class DriverController {

	private final DriverService driverService;
	private final ClientService clientService;
	private final DriverMapper driverMapper;
	private final TelegramWebAppAuthValidator authValidator;

	public DriverController(DriverService driverService, ClientService clientService, DriverMapper driverMapper,
			TelegramWebAppAuthValidator authValidator) {
		this.driverService = driverService;
		this.clientService = clientService;
		this.driverMapper = driverMapper;
		this.authValidator = authValidator;
	}

	
	@PostMapping
	public DriverResponseDTO registerDriver(@RequestBody @Valid DriverCreateDTO driverDTO,
			@RequestHeader("X-Telegram-Init-Data") String initData) {

		// 1. Authenticate user identity via WebApp data
		Long telegramId = authValidator.validate(initData);

		// 2. Retrieve existing client information (assuming every driver is also a
		// client)
		ClientEntity existingClient = clientService.findClientByTelegramId(telegramId);

		DriverEntity driver = driverMapper.fromCreateDto(driverDTO);

		// Rationale: Copy essential client information to the new Driver entity to keep
		// data synchronized.
		driver.setTelegramId(telegramId);
		driver.setFullName(existingClient.getFullName());
		driver.setPhoneNumber(existingClient.getPhoneNumber());
		driver.setTelegramChatId(existingClient.getTelegramChatId());

		DriverEntity registeredDriver = driverService.registerDriver(driver);
		return driverMapper.toResponseDto(registeredDriver);
	}
	
	@PostMapping("/demo-auto-approve")
	@Profile("!prod")
	public ResponseEntity<String> demoAutoApprove(@RequestHeader("X-Telegram-Init-Data") String initData) {
	    Long telegramId = authValidator.validate(initData);
	    DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
	    driverService.adminUpdateDriverStatus(driver.getId(), DriverStatus.ACTIVE);
	    return ResponseEntity.ok("Demo-mode: Driver status successfully changed to ACTIVE");
	}

	@PostMapping("/heartbeat")
	public ResponseEntity<Void> sendHeartbeat(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		driverService.activateDriverByHeartbeat(telegramId);
		return ResponseEntity.ok().build();
	}

	
	@PostMapping("/deactivate")
	public ResponseEntity<Void> deactivateDriver(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		driverService.deactivateDriver(telegramId);
		return ResponseEntity.ok().build();
	}

	
	@PatchMapping("/admin/{id}/status")
	public ResponseEntity<String> adminUpdateDriverStatus(@PathVariable("id") Long driverId,
			@RequestBody @Valid DriverUpdateDTO statusDTO) {

		driverService.adminUpdateDriverStatus(driverId, statusDTO.getStatus());

		return ResponseEntity
				.ok("Driver " + driverId + " administrative status changed to " + statusDTO.getStatus().name());
	}

	
	@GetMapping
	public List<DriverResponseDTO> findAvailableDrivers() {
		List<DriverEntity> availableDrivers = driverService.findAvailableDrivers();
		return availableDrivers.stream().map(driverMapper::toResponseDto).toList();
	}

	
	@GetMapping("/by-telegram-id/{id}")
	public DriverResponseDTO findDriverByTelegramId(@PathVariable("id") Long telegramId) {
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
		return driverMapper.toResponseDto(driver);
	}

	
	@GetMapping("/by-id/{id}")
	public DriverResponseDTO findDriverById(@PathVariable("id") Long driverId) {
		DriverEntity driver = driverService.findDriverById(driverId);
		return driverMapper.toResponseDto(driver);
	}

}
