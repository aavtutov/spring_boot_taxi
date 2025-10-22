package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverCreateDTO {

	@NotBlank(message = "Driver create: Car model is required")
	private String carModel;
	
	@NotBlank(message = "Driver create: Car color is required")
	private String carColor;

	@NotBlank(message = "Driver create: License plate is required")
	private String licensePlate;

	@NotBlank(message = "Driver create: Driver license photo/URL is required")
	private String driverLicenseUrl;

	@NotBlank(message = "Driver create: Car registration photo/URL is required")
	private String carRegistrationUrl;

}
