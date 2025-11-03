package com.aavtutov.spring.boot.spring_boot_taxi.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a driver in the taxi service application.
 *
 * <p>
 * This entity is mapped to the 'drivers' database table and stores personal
 * details, vehicle information, document URLs for verification, and the
 * operational status.
 * </p>
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverEntity {

	/**
	 * The unique primary key (PK) identifier for the driver record. Uses database
	 * identity generation strategy.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/**
	 * The unique Telegram user ID provided by the Telegram API. This field must be
	 * unique and is essential for authentication.
	 */
	@Column(name = "telegram_user_id", unique = true, nullable = false)
	private Long telegramId;

	/**
	 * The unique Telegram Chat ID associated with the driver. Essential for sending
	 * order notifications and status updates via the Telegram Bot API.
	 */
	@Column(name = "telegram_chat_id", unique = true, nullable = false)
	private String telegramChatId;

	/**
	 * The driver's full name.
	 */
	@Column(name = "full_name")
	private String fullName;

	// --- Vehicle Details ---

	/**
	 * The vehicle's make and model (e.g., Toyota Camry).
	 */
	@Column(name = "car_model")
	private String carModel;

	/**
	 * The vehicle's color.
	 */
	@Column(name = "car_color")
	private String carColor;

	/**
	 * The vehicle's unique license plate number. Must be unique in the system.
	 */
	@Column(name = "license_plate", unique = true)
	private String licensePlate;

	/**
	 * The driver's mobile phone number. Must be unique in the system.
	 */
	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	// --- Operational Status and Verification ---

	/**
	 * The current operational status of the driver (e.g., ACTIVE, INACTIVE, BANNED).
	 * Mapped as a string in the database for better readability. Defaults to
	 * {@link DriverStatus#PENDING_APPROVAL} upon creation.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private DriverStatus status = DriverStatus.PENDING_APPROVAL;

	/**
	 * URL link to the driver's license document (for manual verification).
	 */
	@Column(name = "driver_license_url")
	private String driverLicenseUrl;

	/**
	 * URL link to the vehicle registration document (for manual verification).
	 */
	@Column(name = "car_registration_url")
	private String carRegistrationUrl;

	/**
	 * The timestamp indicating when the driver record was created. Automatically
	 * populated by Hibernate upon insertion and cannot be updated.
	 */
	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

}
