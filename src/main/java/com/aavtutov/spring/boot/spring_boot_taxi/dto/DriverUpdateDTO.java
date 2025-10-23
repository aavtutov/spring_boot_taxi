package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) used to update the administrative status of an
 * existing driver.
 *
 * <p>
 * This DTO is typically used by an administrative endpoint or a dedicated
 * service to control a driver's operational state.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverUpdateDTO {

	/**
	 * The new operational status to be assigned to the driver. This field is
	 * required and must correspond to a valid {@link DriverStatus} enum value.
	 */
	@NotNull(message = "Driver status update: status is required")
	private DriverStatus status;

}
