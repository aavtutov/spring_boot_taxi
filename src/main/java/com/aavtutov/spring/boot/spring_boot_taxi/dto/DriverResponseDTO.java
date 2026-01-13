package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public driver profiles and vehicle information.
 * Excludes sensitive document data for API security.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {

	private Long id;

	private Long telegramId;

	private String fullName;

	private String carModel;

	private String carColor;

	private String licensePlate;

}
