package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a driver's operational status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverUpdateDTO {

	@NotNull(message = "Driver status update: status is required")
	private DriverStatus status;

}
