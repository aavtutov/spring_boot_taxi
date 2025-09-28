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
public class DriverCreateDTO {

	@NotNull(message = "Driver create: Telegram ID is required")
	private Long telegramId;

	@NotBlank(message = "Driver create: Phone number is required")
	private String phoneNumber;

	@NotBlank(message = "Driver create: Full name is required")
	private String fullName;

	@NotBlank(message = "Driver create: Car model is required")
	private String carModel;

	@NotBlank(message = "Driver create: License plate is required")
	private String licensePlate;

}
