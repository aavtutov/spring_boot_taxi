package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to signal a status update or action to be
 * performed on an existing order.
 *
 * <p>
 * This object is primarily used in PATCH requests to the /api/orders/{id}
 * endpoint and must specify a valid action from the {@link OrderAction}
 * enumeration.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderUpdateDTO {

	/**
	 * The specific action to be performed on the order (e.g., ACCEPT, COMPLETE,
	 * CANCEL_BY_CLIENT). This field is required and dictates the subsequent
	 * business logic flow.
	 */
	@NotNull(message = "Order update: update requires action")
	private OrderAction action;

	// Rationale: Additional fields (like 'price' for the COMPLETE action, or
	// coordinates for location updates) might be added here in a more complex
	// system, but for now, the action is the sole mandatory input.
}
