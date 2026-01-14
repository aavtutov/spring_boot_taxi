package com.aavtutov.spring.boot.spring_boot_taxi.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence entity representing a client (passenger).
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Unique Telegram user identifier used for authentication. */
	@Column(name = "telegram_user_id", unique = true, nullable = false)
	private Long telegramId;

	/** Chat identifier required for sending bot notifications. */
	@Column(unique = true, nullable = false)
	private String telegramChatId;

	private String fullName;

	@Column(unique = true)
	private String phoneNumber;

	@CreationTimestamp
	@Column(updatable = false, nullable = false)
	private Instant createdAt;

}
