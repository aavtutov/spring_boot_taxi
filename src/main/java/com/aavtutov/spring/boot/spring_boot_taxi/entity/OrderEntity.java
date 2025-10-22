package com.aavtutov.spring.boot.spring_boot_taxi.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.ToString;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private ClientEntity client;

	@ManyToOne
	@JoinColumn(name = "driver_id")
	private DriverEntity driver;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status = OrderStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(name = "cancellation_source")
	private OrderCancellationSource cancellationSource;

	@Column(name = "start_address", nullable = false)
	private String startAddress;

	@Column(name = "end_address", nullable = false)
	private String endAddress;

	@Column(name = "start_latitude", nullable = false, precision = 10, scale = 8)
	private BigDecimal startLatitude;

	@Column(name = "start_longitude", nullable = false, precision = 11, scale = 8)
	private BigDecimal startLongitude;

	@Column(name = "end_latitude", nullable = false, precision = 10, scale = 8)
	private BigDecimal endLatitude;

	@Column(name = "end_longitude", nullable = false, precision = 11, scale = 8)
	private BigDecimal endLongitude;

	@Column(name = "aproximate_distance", precision = 10, scale = 3)
    private BigDecimal aproximateDistance = BigDecimal.ZERO;

	@Column(name = "aproximate_duration", precision = 10, scale = 2)
	private BigDecimal aproximateDuration = BigDecimal.ZERO;

	@Column(name = "actual_duration", precision = 10, scale = 2)
	private BigDecimal actualDuration;
	
	@Column(name = "price", nullable = false)
	private BigDecimal price = BigDecimal.ZERO;

	@Column(name = "bonus_fare", nullable = false)
	private BigDecimal bonusFare = BigDecimal.ZERO;

	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice = BigDecimal.ZERO;
	
	@Column(name = "notes")
	private String notes;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

	@Column(name = "accepted_at")
	private Instant acceptedAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;
	
}
