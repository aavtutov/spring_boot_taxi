package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientCreateDTO {

	@NotNull(message = "Client create: Telegram ID is required")
	private Long telegramId;

	@NotBlank(message = "Client create: Phone number is required")
	private String phoneNumber;

	@NotBlank(message = "Client create: Full name is required")
	private String fullName;

}
