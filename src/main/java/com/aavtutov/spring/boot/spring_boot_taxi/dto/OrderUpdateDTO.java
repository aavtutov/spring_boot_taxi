package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;

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
public class OrderUpdateDTO {

	private Long clientId;
	private Long driverId;
	
	@NotNull(message = "Order update requires action")
	private OrderAction action;
	
	private BigDecimal price;

}
