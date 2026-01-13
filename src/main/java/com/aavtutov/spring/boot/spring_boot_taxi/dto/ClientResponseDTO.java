package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to return client information via API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {

	private Long id;

	private Long telegramId;

	private String fullName;

}
