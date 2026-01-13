package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for driver registration, including vehicle details and document references.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverCreateDTO {

	@NotBlank(message = "Driver create: Car model is required")
	private String carModel;

	@NotBlank(message = "Driver create: Car color is required")
	private String carColor;

	@NotBlank(message = "Driver create: Car license plate is required")
	private String licensePlate;

	@NotBlank(message = "Driver create: Driver license photo/URL is required")
	private String driverLicenseUrl;

	@NotBlank(message = "Driver create: Car registration photo/URL is required")
	private String carRegistrationUrl;
	
	// Helper for title-casing (e.g., "toyota camry" -> "Toyota Camry")
	private String capitalize(String value) {
		if (value == null || value.isBlank()) {
			return value;
		}

		return Arrays.stream(value.trim().toLowerCase().split("\\s+"))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
				.collect(Collectors.joining(" "));
	}
	
	/** Custom setters for data normalization before persistence.
     * This ensures consistent formatting in the database.
     */
	
	// TODO: Move normalization to Service
	
	public void setLicensePlate(String licensePlate) {
		this.licensePlate = (licensePlate == null) ? null : licensePlate.trim().replaceAll("\\s+", " ").toUpperCase();
	}

    public void setCarModel(String carModel) {
        this.carModel = capitalize(carModel);
    }

    public void setCarColor(String carColor) {
        this.carColor = capitalize(carColor);
    }
    
    public void setDriverLicenseUrl(String driverLicenseUrl) {
        this.driverLicenseUrl = (driverLicenseUrl == null) ? null : driverLicenseUrl.trim();
    }

    public void setCarRegistrationUrl(String carRegistrationUrl) {
        this.carRegistrationUrl = (carRegistrationUrl == null) ? null : carRegistrationUrl.trim();
    }


}
