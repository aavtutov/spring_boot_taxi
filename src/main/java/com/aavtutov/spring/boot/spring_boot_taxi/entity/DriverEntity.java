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

/**
 * Persistence entity for drivers.
 * Stores personal data, vehicle specifications, and verification documents.
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "telegram_user_id", unique = true, nullable = false)
	private Long telegramId;

	@Column(unique = true, nullable = false)
	private String telegramChatId;

	private String fullName;
	
	@Column(unique = true)
	private String phoneNumber;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DriverStatus status = DriverStatus.PENDING_APPROVAL;

	// Vehicle Details
	private String carModel;
	private String carColor;

	@Column(unique = true)
	private String licensePlate;

	// Documents
	private String driverLicenseUrl;
	private String carRegistrationUrl;

	@CreationTimestamp
	@Column(updatable = false, nullable = false)
	private Instant createdAt;

}
