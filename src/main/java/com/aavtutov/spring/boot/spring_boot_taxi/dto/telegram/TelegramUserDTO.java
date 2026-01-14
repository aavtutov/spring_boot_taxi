package com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * User information received from the Telegram WebApp's initialization
 * data ('initData').
 */
@Data
public class TelegramUserDTO {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("username")
	private String username;

	// indicates the user is a bot (must be false)
	@JsonProperty("is_bot")
	private Boolean isBot;
}
