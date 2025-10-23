package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to carry necessary data for registering a new
 * client into the system.
 *
 * <p>
 * This object is used as the request body for the client registration API
 * endpoint.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientCreateDTO {

	/**
	 * The unique identifier assigned to the user by Telegram. This field is
	 * required and used to link the client to their Telegram profile.
	 */
	@NotNull(message = "Client create: Telegram ID is required")
	private Long telegramId;

	/**
	 * The client's mobile phone number. This field is required and must not be
	 * blank.
	 */
	@NotBlank(message = "Client create: Phone number is required")
	private String phoneNumber;

	/**
	 * The client's full name (usually copied from the Telegram profile). This field
	 * is required and must not be blank.
	 */
	@NotBlank(message = "Client create: Full name is required")
	private String fullName;

}
