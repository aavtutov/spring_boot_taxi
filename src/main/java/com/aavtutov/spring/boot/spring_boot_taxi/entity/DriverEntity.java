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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "telegram_user_id", unique = true, nullable = false)
	private Long telegramId;

	@Column(name = "telegram_chat_id", unique = true, nullable = false)
	private String telegramChatId;

	@Column(name = "full_name")
	private String fullName;

	// --- Vehicle Details ---

	@Column(name = "car_model")
	private String carModel;

	@Column(name = "car_color")
	private String carColor;

	@Column(name = "license_plate", unique = true)
	private String licensePlate;

	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	// --- Operational Status and Verification ---

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private DriverStatus status = DriverStatus.PENDING_APPROVAL;

	@Column(name = "driver_license_url")
	private String driverLicenseUrl;

	@Column(name = "car_registration_url")
	private String carRegistrationUrl;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

}
