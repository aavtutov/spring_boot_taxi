package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
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
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

	private final DriverService driverService;
	private final DriverMapper driverMapper;

    @GetMapping("/me")
    public DriverResponseDTO getCurrentDriver(ClientEntity client) {
        DriverEntity driver = driverService.findDriverByTelegramId(client.getTelegramId());
        return driverMapper.toResponseDto(driver);
    }
	
    /**
     * Registers a new driver using authenticated Client data.
     * Essential client fields (Telegram ID, Phone, Name) are mapped from the ClientEntity 
     * to avoid spoofing via DTO.
     */
	@PostMapping
	public DriverResponseDTO registerDriver(@RequestBody @Valid DriverCreateDTO driverDTO,
			ClientEntity client) {

		DriverEntity driver = driverMapper.fromCreateDto(driverDTO);

		driver.setTelegramId(client.getTelegramId());
		driver.setFullName(client.getFullName());
		driver.setPhoneNumber(client.getPhoneNumber());
		driver.setTelegramChatId(client.getTelegramChatId());

		DriverEntity registeredDriver = driverService.registerDriver(driver);
		return driverMapper.toResponseDto(registeredDriver);
	}
	
	/**
     * Updates driver's last activity and sets status to ACTIVE.
     * Should be called periodically by the frontend to stay visible on the map.
     */
	@PostMapping("/heartbeat")
	public ResponseEntity<Void> sendHeartbeat(DriverEntity driver) {
		driverService.activateDriverByHeartbeat(driver);
		return ResponseEntity.ok().build();
	}

	/**
     * Manually sets driver status to OFFLINE.
     * Used when the driver chooses to stop receiving new order requests.
     */
	@PostMapping("/deactivate")
	public ResponseEntity<Void> deactivateDriver(DriverEntity driver) {
		driverService.deactivateDriver(driver.getTelegramId());
		return ResponseEntity.ok().build();
	}
	
	/**
     * Development-only endpoint to bypass administrative review.
     */
	@PostMapping("/demo-auto-approve")
	@Profile("!prod")
	public ResponseEntity<String> demoAutoApprove(DriverEntity driver) {
	    driverService.adminUpdateDriverStatus(driver.getId(), DriverStatus.ACTIVE);
	    return ResponseEntity.ok("Demo-mode: Driver status activated");
	}
	
	@GetMapping
	public List<DriverResponseDTO> findAvailableDrivers() {
		List<DriverEntity> availableDrivers = driverService.findAvailableDrivers();
		return availableDrivers.stream().map(driverMapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public DriverResponseDTO findDriverById(@PathVariable("id") Long driverId) {
		DriverEntity driver = driverService.findDriverById(driverId);
		return driverMapper.toResponseDto(driver);
	}
	
	@PatchMapping("/admin/{id}/status")
	public ResponseEntity<String> adminUpdateDriverStatus(@PathVariable("id") Long driverId,
			@RequestBody @Valid DriverUpdateDTO statusDTO) {

		driverService.adminUpdateDriverStatus(driverId, statusDTO.getStatus());
		return ResponseEntity.ok("Status updated to " + statusDTO.getStatus().name());
	}
}
