package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to return essential driver and vehicle
 * information in API responses (e.g., when showing available drivers or order
 * details).
 *
 * <p>
 * Exposes vehicle details necessary for identification by the client (e.g.,
 * license plate) but omits sensitive internal data.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverResponseDTO {

	/**
	 * The unique primary key identifier of the driver in the database. This is the
	 * internal system ID.
	 */
	private Long id;

	/**
	 * The unique identifier assigned to the user by Telegram.
	 */
	private Long telegramId;

	/**
	 * The driver's full name.
	 */
	private String fullName;

	/**
	 * The make and model of the driver's vehicle (e.g., "Toyota Camry").
	 */
	private String carModel;

	/**
	 * The color of the driver's vehicle.
	 */
	private String carColor;

	/**
	 * The vehicle's unique license plate number.
	 */
	private String licensePlate;

}
