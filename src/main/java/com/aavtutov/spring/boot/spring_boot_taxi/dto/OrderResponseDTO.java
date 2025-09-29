package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

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
public class OrderResponseDTO {

	private Long id;

	private ClientResponseDTO client;
	private DriverResponseDTO driver;

	private OrderStatus status;

	private String startAddress;
	private String endAddress;

	private BigDecimal startLatitude;
	private BigDecimal startLongitude;
	private BigDecimal endLatitude;
	private BigDecimal endLongitude;

	private BigDecimal price;
	private BigDecimal bonusFare;

	private String mapScreenshotUrl;
	private String locationPhotoUrl;

	private Instant createdAt;
	private Instant acceptedAt;
	private Instant startedAt;
	private Instant completedAt;
	private Instant cancelledAt;
	
	private OrderCancellationSource cancellationSource;
	
	private BigDecimal totalPrice;

}
