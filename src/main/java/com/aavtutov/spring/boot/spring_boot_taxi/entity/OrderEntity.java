package com.aavtutov.spring.boot.spring_boot_taxi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence entity tracking the full lifecycle of a taxi order.
 * Manages route coordinates, pricing, and status transitions from creation to completion.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Relationships
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private ClientEntity client;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id")
	private DriverEntity driver;

	// Status and Cancellation
	@Enumerated(EnumType.STRING)
	private OrderStatus status = OrderStatus.PENDING;

	@Enumerated(EnumType.STRING)
	private OrderCancellationSource cancellationSource;

	// Route Details
	@Column(nullable = false)
	private String startAddress;

	@Column(nullable = false)
	private String endAddress;

	@Column(nullable = false, precision = 10, scale = 8)
	private BigDecimal startLatitude;

	@Column(nullable = false, precision = 11, scale = 8)
	private BigDecimal startLongitude;

	@Column(nullable = false, precision = 10, scale = 8)
	private BigDecimal endLatitude;

	@Column(nullable = false, precision = 11, scale = 8)
	private BigDecimal endLongitude;

	// Estimation and Pricing
	@Column(precision = 10, scale = 3)
	private BigDecimal aproximateDistance = BigDecimal.ZERO;

	@Column(precision = 10, scale = 2)
	private BigDecimal aproximateDuration = BigDecimal.ZERO;

	@Column(precision = 10, scale = 2)
	private BigDecimal actualDuration;

	@Column(nullable = false)
	private BigDecimal price = BigDecimal.ZERO;

	@Column(nullable = false)
	private BigDecimal bonusFare = BigDecimal.ZERO;

	@Column(nullable = false)
	private BigDecimal totalPrice = BigDecimal.ZERO;

	private String notes;

	// Lifecycle Timestamps
	@CreationTimestamp
	@Column(updatable = false, nullable = false)
	private Instant createdAt;

	private Instant acceptedAt;
	private Instant startedAt;
	private Instant completedAt;
	private Instant cancelledAt;

}
