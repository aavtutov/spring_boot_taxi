package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
	
	@NotBlank(message = "Order create: Start Address is required")
	private String startAddress;
	
	@NotBlank(message = "Order create: End Address is required")
	private String endAddress;

//  @DecimalMin(value = "-90.0", message = "Order create: Start Longitude must be >= -90")
//  @DecimalMax(value = "90.0", message = "Order create: Start Longitude must be <= 90")
	@NotNull(message = "Order create Start Latitude is required")
	private BigDecimal startLatitude;
	
//  @DecimalMin(value = "-180.0", message = "Order create: Start Longitude must be >= -180")
//  @DecimalMax(value = "180.0", message = "Order create: Start Longitude must be <= 180")
	@NotNull(message = "Order create: Start Longitude is required")
	private BigDecimal startLongitude;
	
//  @DecimalMin(value = "-90.0", message = "Order create: End Latitude must be >= -90")
//  @DecimalMax(value = "90.0", message = "Order create: End Latitude must be <= 90")
	@NotNull(message = "Order create: End Latitude is required")
	private BigDecimal endLatitude;
	
//  @DecimalMin(value = "-180.0", message = "Order create: End Longitude must be >= -180")
//  @DecimalMax(value = "180.0", message = "Order create: End Longitude must be <= 180")
	@NotNull(message = "Order create: End Longitude is required")
	private BigDecimal endLongitude;

	@DecimalMin(value = "0.0", inclusive = false, message = "Order create: Price must be > 0")
	@NotNull(message = "Order create: Price is required")
	private BigDecimal price;
	
	@DecimalMin(value = "0.0", inclusive = true, message = "Order create: Bonus fare must be >= 0")
	@NotNull(message = "Order create: Bonus fare is required")
	private BigDecimal bonusFare;

//	@URL(message = "Order create: Invalid map screenshot URL")
	private String mapScreenshotUrl;
	
//	@URL(message = "Order create: Invalid location photo URL")
	private String locationPhotoUrl;

}
