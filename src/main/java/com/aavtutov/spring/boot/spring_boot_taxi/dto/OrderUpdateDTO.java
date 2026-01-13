package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting order status transitions and lifecycle actions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {

	@NotNull(message = "Order update: update requires action")
	private OrderAction action;

}
