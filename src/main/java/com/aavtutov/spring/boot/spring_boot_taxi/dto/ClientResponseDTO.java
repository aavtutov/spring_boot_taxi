package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to return essential client information in API
 * responses.
 *
 * <p>
 * It exposes internal identifiers (like primary key ID) and basic public
 * details, but omits sensitive information (like phone number).
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientResponseDTO {

	/**
	 * The unique primary key identifier of the client in the database. This is the
	 * internal system ID.
	 */
	private Long id;

	/**
	 * The unique identifier assigned to the user by Telegram. This is the external,
	 * immutable ID.
	 */
	private Long telegramId;

	/**
	 * The client's full name, typically copied from their Telegram profile.
	 */
	private String fullName;

}
