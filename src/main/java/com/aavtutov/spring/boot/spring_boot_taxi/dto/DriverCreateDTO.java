package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to collect the necessary vehicle and document
 * information for a user registering as a driver.
 *
 * <p>
 * This object is typically used in conjunction with the user's existing client
 * identity (Telegram ID).
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverCreateDTO {

	/**
	 * The make and model of the driver's vehicle (e.g., "Toyota Camry"). This field
	 * is required.
	 */
	@NotBlank(message = "Driver create: Car model is required")
	private String carModel;

	/**
	 * The color of the driver's vehicle. This field is required.
	 */
	@NotBlank(message = "Driver create: Car color is required")
	private String carColor;

	/**
	 * The unique license plate number of the vehicle. This field is required.
	 */
	@NotBlank(message = "Driver create: Car license plate is required")
	private String licensePlate;

	/**
	 * URL link to the photo or scan of the driver's license document. This field is
	 * required for validation/approval.
	 */
	@NotBlank(message = "Driver create: Driver license photo/URL is required")
	private String driverLicenseUrl;

	/**
	 * URL link to the photo or scan of the vehicle registration document. This
	 * field is required for vehicle verification.
	 */
	@NotBlank(message = "Driver create: Car registration photo/URL is required")
	private String carRegistrationUrl;

}
