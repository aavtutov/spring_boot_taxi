package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed information about a taxi order, including participants, 
 * route coordinates, and lifecycle timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

	private Long id;

	private ClientResponseDTO client;

	private DriverResponseDTO driver;

	private OrderStatus status;

	// --- Address and Coordinates ---

	private String startAddress;

	private String endAddress;

	private BigDecimal startLatitude;

	private BigDecimal startLongitude;

	private BigDecimal endLatitude;

	private BigDecimal endLongitude;

	// --- Route and Price Estimates ---

	private BigDecimal aproximateDistance;

	private BigDecimal aproximateDuration;
	
	private BigDecimal actualDuration;

	private BigDecimal price;

	private BigDecimal bonusFare;

	// --- Lifecycle Timestamps ---

	private Instant createdAt;

	private Instant acceptedAt;

	private Instant startedAt;

	private Instant completedAt;

	private Instant cancelledAt;

	// --- Final Details ---

	private OrderCancellationSource cancellationSource;

	private BigDecimal totalPrice;

	private String notes;

}
