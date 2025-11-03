package com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) representing the user information received from
 * the Telegram WebApp's initialization data ('initData').
 *
 * <p>
 * This DTO is crucial for authenticating and identifying the user interacting
 * with the taxi service, using the official Telegram data structure.
 * </p>
 */
@Getter
@Setter
@ToString
public class TelegramUserDTO {

	/**
	 * The unique Telegram ID of the user. Mapped from 'id'.
	 */
	@JsonProperty("id")
	private Long id;

	/**
	 * The user's first name. Mapped from 'first_name'.
	 */
	@JsonProperty("first_name")
	private String firstName;

	/**
	 * The user's Telegram username, if available. Mapped from 'username'.
	 */
	@JsonProperty("username")
	private String username;

	/**
	 * A flag indicating if the user is a bot. Must be {@code false} for legitimate
	 * client interactions. Mapped from 'is_bot'.
	 */
	// indicates the user is a bot (must be false)
	@JsonProperty("is_bot")
	private Boolean isBot;
}
