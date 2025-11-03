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

/**
 * Represents a single taxi order in the system, tracking its full lifecycle
 * from creation to completion or cancellation.
 *
 * <p>
 * This entity is mapped to the 'orders' database table and manages
 * relationships to the client, the assigned driver, geographic coordinates,
 * pricing, and temporal status changes.
 * </p>
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderEntity {

	/**
	 * The unique primary key (PK) identifier for the order record. Uses database
	 * identity generation strategy.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// --- Relationships ---

	/**
	 * The {@link ClientEntity} who placed the order. This relationship is mandatory
	 * (not nullable).
	 */
	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private ClientEntity client;

	/**
	 * The {@link DriverEntity} assigned to the order. This field is nullable until
	 * an order is accepted by a driver.
	 */
	@ManyToOne
	@JoinColumn(name = "driver_id")
	private DriverEntity driver;

	// --- Status and Cancellation ---

	/**
	 * The current status of the order. Mapped as a string in the database. Defaults
	 * to {@link OrderStatus#PENDING} upon creation.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus status = OrderStatus.PENDING;

	/**
	 * The party (Client or Driver) that initiated the cancellation, if applicable.
	 * Null if the order was completed or is still active.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "cancellation_source")
	private OrderCancellationSource cancellationSource;

	// --- Route Details (Addresses) ---

	/**
	 * The human-readable starting address (pickup location). Cannot be null.
	 */
	@Column(name = "start_address", nullable = false)
	private String startAddress;

	/**
	 * The human-readable destination address (drop-off location). Cannot be null.
	 */
	@Column(name = "end_address", nullable = false)
	private String endAddress;

	// --- Route Details (Geographic Coordinates) ---

	/**
	 * The starting latitude coordinate. Cannot be null. Precision (10, 8) allows
	 * for up to 8 decimal places (e.g., 99.99999999).
	 */
	@Column(name = "start_latitude", nullable = false, precision = 10, scale = 8)
	private BigDecimal startLatitude;

	/**
	 * The starting longitude coordinate. Cannot be null. Precision (11, 8) allows
	 * for up to 8 decimal places (e.g., 199.99999999).
	 */
	@Column(name = "start_longitude", nullable = false, precision = 11, scale = 8)
	private BigDecimal startLongitude;

	/**
	 * The destination latitude coordinate. Cannot be null.
	 */
	@Column(name = "end_latitude", nullable = false, precision = 10, scale = 8)
	private BigDecimal endLatitude;

	/**
	 * The destination longitude coordinate. Cannot be null.
	 */
	@Column(name = "end_longitude", nullable = false, precision = 11, scale = 8)
	private BigDecimal endLongitude;

	// --- Estimation and Pricing ---

	/**
	 * The estimated distance of the route. Defaults to 0. Precision (10, 3) allows
	 * for up to 3 decimal places (e.g., in kilometers).
	 */
	@Column(name = "aproximate_distance", precision = 10, scale = 3)
	private BigDecimal aproximateDistance = BigDecimal.ZERO;

	/**
	 * The estimated duration of the trip (in minutes). Defaults to 0. Precision
	 * (10, 2) allows for up to 2 decimal places.
	 */
	@Column(name = "aproximate_duration", precision = 10, scale = 2)
	private BigDecimal aproximateDuration = BigDecimal.ZERO;

	/**
	 * The actual duration of the trip upon completion (in minutes). Null until
	 * completion.
	 */
	@Column(name = "actual_duration", precision = 10, scale = 2)
	private BigDecimal actualDuration;

	/**
	 * The calculated base price of the ride. Cannot be null. Defaults to 0 (though
	 * business logic should set > 0).
	 */
	@Column(name = "price", nullable = false)
	private BigDecimal price = BigDecimal.ZERO;

	/**
	 * The optional bonus or tip offered by the client. Cannot be null. Defaults to
	 * 0.
	 */
	@Column(name = "bonus_fare", nullable = false)
	private BigDecimal bonusFare = BigDecimal.ZERO;

	/**
	 * The final calculated total price of the order (price + bonus + adjustments).
	 * Cannot be null. Defaults to 0.
	 */
	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice = BigDecimal.ZERO;

	/**
	 * Any specific notes or requests from the client. Nullable.
	 */
	@Column(name = "notes")
	private String notes;

	// --- Lifecycle Timestamps ---

	/**
	 * The timestamp when the order was created. Automatically set and
	 * non-updatable.
	 */
	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

	/**
	 * The timestamp when a driver accepted the order. Null until accepted.
	 */
	@Column(name = "accepted_at")
	private Instant acceptedAt;

	/**
	 * The timestamp when the trip officially started (client pickup). Null until
	 * started.
	 */
	@Column(name = "started_at")
	private Instant startedAt;

	/**
	 * The timestamp when the trip was completed. Null until completed.
	 */
	@Column(name = "completed_at")
	private Instant completedAt;

	/**
	 * The timestamp when the order was cancelled. Null until cancelled.
	 */
	@Column(name = "cancelled_at")
	private Instant cancelledAt;

}
