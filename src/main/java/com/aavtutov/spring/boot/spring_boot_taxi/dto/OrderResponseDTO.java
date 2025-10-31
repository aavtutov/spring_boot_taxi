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

/**
 * Data Transfer Object (DTO) used to return the complete, detailed state of a
 * taxi order.
 *
 * <p>
 * This includes client and driver details, geographic coordinates, pricing, and
 * a full timeline of the order's lifecycle (creation, acceptance, completion,
 * cancellation).
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderResponseDTO {

	/**
	 * The unique primary key identifier of the order in the database.
	 */
	private Long id;

	/**
	 * Details of the client who placed the order.
	 */
	private ClientResponseDTO client;

	/**
	 * Details of the driver assigned to the order, if one has been assigned.
	 */
	private DriverResponseDTO driver;

	/**
	 * The current status of the order (e.g., PENDING, ACCEPTED, COMPLETED).
	 */
	private OrderStatus status;

	// --- Address and Coordinates ---

	/**
	 * The human-readable starting address (pickup location).
	 */
	private String startAddress;

	/**
	 * The human-readable destination address (drop-off location).
	 */
	private String endAddress;

	/**
	 * The starting latitude coordinate (for map display).
	 */
	private BigDecimal startLatitude;

	/**
	 * The starting longitude coordinate (for map display).
	 */
	private BigDecimal startLongitude;

	/**
	 * The destination latitude coordinate (for map display).
	 */
	private BigDecimal endLatitude;

	/**
	 * The destination longitude coordinate (for map display).
	 */
	private BigDecimal endLongitude;

	// --- Route and Price Estimates ---

	/**
	 * The estimated distance of the planned route (in kilometers or miles).
	 */
	private BigDecimal aproximateDistance;

	/**
	 * The estimated duration of the planned trip (in minutes).
	 */
	private BigDecimal aproximateDuration;
	
	/**
	 * The actual duration of the completed trip (in minutes).
	 */
	private BigDecimal actualDuration;

	/**
	 * The initial base price of the ride, calculated upon creation.
	 */
	private BigDecimal price;

	/**
	 * The optional bonus or tip offered by the client.
	 */
	private BigDecimal bonusFare;

	// --- Lifecycle Timestamps ---

	/**
	 * The timestamp when the order was first placed and entered the system.
	 */
	private Instant createdAt;

	/**
	 * The timestamp when a driver accepted the order. Null if the order has not
	 * been accepted or was cancelled beforehand.
	 */
	private Instant acceptedAt;

	/**
	 * The timestamp when the driver signaled the start of the trip (client picked
	 * up). Null if the trip has not started.
	 */
	private Instant startedAt;

	/**
	 * The timestamp when the driver signaled the completion of the trip. Null if
	 * the trip is not completed.
	 */
	private Instant completedAt;

	/**
	 * The timestamp when the order was cancelled. Null if the order was completed
	 * or is still active.
	 */
	private Instant cancelledAt;

	// --- Final Details ---

	/**
	 * The party that initiated the cancellation, if the order was cancelled. Null
	 * if the order was completed or is still active.
	 */
	private OrderCancellationSource cancellationSource;

	/**
	 * The final calculated total price, including any initial price, bonuses.
	 */
	private BigDecimal totalPrice;

	/**
	 * Any specific notes or requests from the client.
	 */
	private String notes;

}
