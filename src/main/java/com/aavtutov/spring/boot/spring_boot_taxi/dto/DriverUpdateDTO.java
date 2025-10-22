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

	@NotNull(message = "Driver requires status")
	private DriverStatus status;

}
