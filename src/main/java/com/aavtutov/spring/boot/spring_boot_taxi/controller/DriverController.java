package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

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
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;

import jakarta.validation.Valid;

/**
 * REST Controller for managing driver-related operations and their status
 * updates.
 *
 * <p>
 * Endpoints are secured using the {@code X-Telegram-Init-Data} header for
 * authentication of requests coming from the Driver's WebApp interface.
 * </p>
 */
@RestController
@RequestMapping("/api/drivers")
public class DriverController {

	private final DriverService driverService;
	private final ClientService clientService;
	private final DriverMapper driverMapper;
	private final TelegramWebAppAuthValidator authValidator;

	/**
	 * Constructs the DriverController, injecting all necessary services and the
	 * security validator.
	 */
	public DriverController(DriverService driverService, ClientService clientService, DriverMapper driverMapper,
			TelegramWebAppAuthValidator authValidator) {
		this.driverService = driverService;
		this.clientService = clientService;
		this.driverMapper = driverMapper;
		this.authValidator = authValidator;
	}

	/**
	 * Registers a new driver in the system.
	 *
	 * <p>
	 * Requires a valid Telegram WebApp authentication header to verify the user
	 * identity and retrieve existing client details for initialization.
	 * </p>
	 * <p>
	 * Endpoint: POST /api/drivers
	 * </p>
	 *
	 * @param driverDTO The request body containing new driver details.
	 * @param initData  The authentication header from the Telegram WebApp.
	 * @return The created driver's details as a response DTO.
	 */
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

	/**
	 * Updates the driver's availability status and location (heartbeat).
	 *
	 * <p>
	 * Used by the WebApp to signal that the driver is online and active.
	 * </p>
	 * <p>
	 * Endpoint: POST /api/drivers/heartbeat
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp.
	 * @return 200 OK with no content.
	 */
	@PostMapping("/heartbeat")
	public ResponseEntity<Void> sendHeartbeat(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		driverService.activateDriverByHeartbeat(telegramId);
		return ResponseEntity.ok().build();
	}

	/**
	 * Deactivates the driver, changing their status to offline/unavailable.
	 *
	 * <p>
	 * Endpoint: POST /api/drivers/deactivate
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp.
	 * @return 200 OK with no content.
	 */
	@PostMapping("/deactivate")
	public ResponseEntity<Void> deactivateDriver(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		driverService.deactivateDriver(telegramId);
		return ResponseEntity.ok().build();
	}

	/**
	 * Administrative endpoint to manually update a driver's status (e.g., block,
	 * ban, manually activate).
	 *
	 * <p>
	 * This endpoint is typically restricted to admin roles.
	 * </p>
	 * <p>
	 * Endpoint: PATCH /api/drivers/{id}/admin/status
	 * </p>
	 *
	 * @param driverId  The primary key ID of the driver to update.
	 * @param statusDTO The request body containing the new status value.
	 * @return 200 OK with a confirmation message.
	 */
	@PatchMapping("/{id}/admin/status")
	public ResponseEntity<String> adminUpdateDriverStatus(@PathVariable("id") Long driverId,
			@RequestBody @Valid DriverUpdateDTO statusDTO) {

		driverService.adminUpdateDriverStatus(driverId, statusDTO.getStatus());

		return ResponseEntity
				.ok("Driver " + driverId + " administrative status changed to " + statusDTO.getStatus().name());
	}

	/**
	 * Retrieves a list of all drivers currently marked as available for accepting
	 * orders.
	 *
	 * <p>
	 * Endpoint: GET /api/drivers
	 * </p>
	 *
	 * @return A list of {@link DriverResponseDTO} for available drivers.
	 */
	@GetMapping
	public List<DriverResponseDTO> findAvailableDrivers() {
		List<DriverEntity> availableDrivers = driverService.findAvailableDrivers();
		return availableDrivers.stream().map(driverMapper::toResponseDto).toList();
	}

	/**
	 * Retrieves driver details by their unique Telegram ID.
	 *
	 * <p>
	 * Endpoint: GET /api/drivers/by-telegram-id/{id}
	 * </p>
	 *
	 * @param telegramId The unique ID assigned by Telegram.
	 * @return The driver's details as a response DTO.
	 */
	@GetMapping("/by-telegram-id/{id}")
	public DriverResponseDTO findDriverByTelegramId(@PathVariable("id") Long telegramId) {
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
		return driverMapper.toResponseDto(driver);
	}

	/**
	 * Retrieves driver details by their unique primary key ID.
	 *
	 * <p>
	 * Endpoint: GET /api/drivers/by-id/{id}
	 * </p>
	 *
	 * @param driverId The primary key ID of the driver in the database.
	 * @return The driver's details as a response DTO.
	 */
	@GetMapping("/by-id/{id}")
	public DriverResponseDTO findDriverById(@PathVariable("id") Long driverId) {
		DriverEntity driver = driverService.findDriverById(driverId);
		return driverMapper.toResponseDto(driver);
	}

}
