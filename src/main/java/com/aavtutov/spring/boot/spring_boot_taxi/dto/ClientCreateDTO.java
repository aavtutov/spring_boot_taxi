package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for new client registration requests via the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDTO {

	@NotNull(message = "Client create: Telegram ID is required")
	private Long telegramId;

	@NotBlank(message = "Client create: Phone number is required")
	private String phoneNumber;

	@NotBlank(message = "Client create: Full name is required")
	private String fullName;

}
