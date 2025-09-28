package com.aavtutov.spring.boot.spring_boot_taxi.dto;

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
public class DriverResponseDTO {

	private Long id;
	
	private Long telegramId;
	
	private String fullName;

	private String carModel;

	private String licensePlate;
	
}
