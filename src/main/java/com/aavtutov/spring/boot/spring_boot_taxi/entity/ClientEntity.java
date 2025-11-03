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
import lombok.ToString;

/**
 * Represents a client (passenger) in the taxi service application.
 *
 * <p>
 * This entity is mapped to the 'clients' database table and stores
 * authentication details (Telegram IDs) and contact information.
 * </p>
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientEntity {

	/**
	 * The unique primary key (PK) identifier for the client record. Uses database
	 * identity generation strategy.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/**
	 * The unique Telegram user ID provided by the Telegram API. This field must be
	 * unique and cannot be null, serving as a primary external identifier.
	 */
	@Column(name = "telegram_user_id", unique = true, nullable = false)
	private Long telegramId;

	/**
	 * The unique Telegram Chat ID associated with the client. This ID is essential
	 * for sending direct notifications or messages back to the user via the
	 * Telegram Bot API. It must be unique and cannot be null.
	 */
	@Column(name = "telegram_chat_id", unique = true, nullable = false)
	private String telegramChatId;

	/**
	 * The client's full name, typically retrieved from their Telegram profile.
	 */
	@Column(name = "full_name")
	private String fullName;

	/**
	 * The client's mobile phone number. It is optional but must be unique if
	 * provided, ensuring only one client registers with a given number.
	 */
	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	/**
	 * The timestamp indicating when the client record was created in the database.
	 * Automatically populated by Hibernate upon insertion and cannot be updated.
	 */
	@CreationTimestamp
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

}
