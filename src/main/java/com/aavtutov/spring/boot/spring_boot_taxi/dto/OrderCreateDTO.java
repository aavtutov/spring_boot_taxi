package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new taxi order.
 * Uses {@link BigDecimal} for geographic coordinates and currency to ensure high precision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

	@NotBlank(message = "Order create: Start Address is required")
	private String startAddress;

	@NotBlank(message = "Order create: End Address is required")
	private String endAddress;

	@DecimalMin("-90.0") @DecimalMax("90.0")
	@NotNull(message = "Order create Start Latitude is required")
	private BigDecimal startLatitude;

	@DecimalMin("-180.0") @DecimalMax("180.0")
	@NotNull(message = "Order create: Start Longitude is required")
	private BigDecimal startLongitude;

	@DecimalMin("-90.0") @DecimalMax("90.0")
	@NotNull(message = "Order create: End Latitude is required")
	private BigDecimal endLatitude;

	@DecimalMin("-180.0") @DecimalMax("180.0")
	@NotNull(message = "Order create: End Longitude is required")
	private BigDecimal endLongitude;

	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
	@NotNull(message = "Order create: Price is required")
	private BigDecimal price;

	@DecimalMin(value = "0.0", message = "Bonus fare cannot be negative")
	private BigDecimal bonusFare;

	private String notes;

}
