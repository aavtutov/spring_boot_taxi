package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;

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
public class DriverUpdateDTO {

	/**
	 * The new operational status to be assigned to the driver. This field is
	 * required and must correspond to a valid {@link DriverStatus} enum value.
	 */
	@NotNull(message = "Driver status update: status is required")
	private DriverStatus status;

}
