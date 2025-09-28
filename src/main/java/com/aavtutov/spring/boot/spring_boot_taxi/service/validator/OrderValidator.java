package com.aavtutov.spring.boot.spring_boot_taxi.service.validator;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.AccessDeniedException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverOfflineException;

@Component
public class OrderValidator {

	public void throwIfOrderStatusNotCancellable(OrderEntity order) {
		if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.COMPLETED) {
			throw new IllegalStateException("Order cannot be cancelled in its current status: " + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotCompletable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.IN_PROGRESS) {
			throw new IllegalStateException("Order cannot be completed in its current status: " + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotAcceptable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new IllegalStateException(
					"Only PENDING orders can be accepted. Current status: " + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotStartable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.ACCEPTED) {
			throw new IllegalStateException(
					"Only ACCEPTED orders can be started. Current status: " + order.getStatus());
		}
	}

	public void throwIfDriverNotAssignedToOrder(OrderEntity order, Long driverId) {
		if (order.getDriver() == null || !order.getDriver().getId().equals(driverId)) {
			throw new AccessDeniedException("Driver with id=" + driverId + " is not assigned to this order.");
		}
	}

	public void throwIfClientNotAssignedToOrder(OrderEntity order, Long clientId) {
		if (order.getClient() == null || !order.getClient().getId().equals(clientId)) {
			throw new AccessDeniedException("Client with id=" + clientId + " is not assigned to this order.");
		}
	}

	public void throwIfDriverOffline(DriverEntity driver) {
		if (!Boolean.TRUE.equals(driver.getIsOnline())) {
			throw new DriverOfflineException("Driver with id=" + driver.getId() + " is offline.");
		}
	}

}
