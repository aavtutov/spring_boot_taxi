package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) used to carry all necessary data for a client
 * placing a new taxi order.
 *
 * <p>
 * Includes address information, precise geographic coordinates (using
 * BigDecimal for accuracy), and initial pricing details.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

	/**
	 * The human-readable starting address (pickup location). This field is
	 * required.
	 */
	@NotBlank(message = "Order create: Start Address is required")
	private String startAddress;

	/**
	 * The human-readable destination address (drop-off location). This field is
	 * required.
	 */
	@NotBlank(message = "Order create: End Address is required")
	private String endAddress;

	/**
	 * The starting latitude coordinate. Must be between -90.0 and 90.0. Required.
	 * Stored as BigDecimal for high precision.
	 */
	@DecimalMin(value = "-90.0", message = "Order create: Start Longitude must be >= -90")
	@DecimalMax(value = "90.0", message = "Order create: Start Longitude must be <= 90")
	@NotNull(message = "Order create Start Latitude is required")
	private BigDecimal startLatitude;

	/**
	 * The starting longitude coordinate. Must be between -180.0 and 180.0.
	 * Required. Stored as BigDecimal for high precision.
	 */
	@DecimalMin(value = "-180.0", message = "Order create: Start Longitude must be >= -180")
	@DecimalMax(value = "180.0", message = "Order create: Start Longitude must be <= 180")
	@NotNull(message = "Order create: Start Longitude is required")
	private BigDecimal startLongitude;

	/**
	 * The destination latitude coordinate. Must be between -90.0 and 90.0.
	 * Required. Stored as BigDecimal for high precision.
	 */
	@DecimalMin(value = "-90.0", message = "Order create: End Latitude must be >= -90")
	@DecimalMax(value = "90.0", message = "Order create: End Latitude must be <= 90")
	@NotNull(message = "Order create: End Latitude is required")
	private BigDecimal endLatitude;

	/**
	 * The destination longitude coordinate. Must be between -180.0 and 180.0.
	 * Required. Stored as BigDecimal for high precision.
	 */
	@DecimalMin(value = "-180.0", message = "Order create: End Longitude must be >= -180")
	@DecimalMax(value = "180.0", message = "Order create: End Longitude must be <= 180")
	@NotNull(message = "Order create: End Longitude is required")
	private BigDecimal endLongitude;

	/**
	 * The calculated initial price of the ride. Must be greater than 0. Required.
	 * Stored as BigDecimal for currency calculations.
	 */
	@DecimalMin(value = "0.0", inclusive = false, message = "Order create: Price must be > 0")
	@NotNull(message = "Order create: Price is required")
	private BigDecimal price;

	/**
	 * An optional bonus or tip offered by the client, added to the final driver
	 * payout. Must be greater than or equal to 0.
	 */
	@DecimalMin(value = "0.0", inclusive = true, message = "Order create: Bonus fare must be >= 0")
	private BigDecimal bonusFare;

	/**
	 * Any specific notes or requests from the client regarding the ride (e.g.,
	 * "luggage", "quiet driver"). Optional field.
	 */
	private String notes;

}
