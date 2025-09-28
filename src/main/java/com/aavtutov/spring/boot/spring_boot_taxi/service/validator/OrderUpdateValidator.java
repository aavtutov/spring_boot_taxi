package com.aavtutov.spring.boot.spring_boot_taxi.service.validator;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderAction;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderUpdateDTO;

@Component
public class OrderUpdateValidator {

	private void throwIfDriverNull(OrderUpdateDTO updateDTO) {
		if (updateDTO.getDriverId() == null) {
			throw new IllegalArgumentException("Action " + updateDTO.getAction() + " requires driverId");
		}
	}

	private void throwIfClientNull(OrderUpdateDTO updateDTO) {
		if (updateDTO.getClientId() == null) {
			throw new IllegalArgumentException("Action " + updateDTO.getAction() + " requires clientId");
		}
	}

	private void throwIfPriceNull(OrderUpdateDTO updateDTO) {
		if (updateDTO.getPrice() == null) {
			throw new IllegalArgumentException("Action " + updateDTO.getAction() + " requires price");
		}
	}

	public void validate(OrderUpdateDTO updateDTO) {

		OrderAction action = updateDTO.getAction();

		switch (action) {

		case COMPLETE -> {
			throwIfDriverNull(updateDTO);
			throwIfPriceNull(updateDTO);
		}

		case ACCEPT, START_TRIP, CANCEL_BY_DRIVER -> {
			throwIfDriverNull(updateDTO);
		}

		case CANCEL_BY_CLIENT -> {
			throwIfClientNull(updateDTO);
		}

		default -> throw new IllegalArgumentException("Unsupported action: " + action);

		}

	}

}
