package com.aavtutov.spring.boot.spring_boot_taxi.service.validator;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.AccessDeniedException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderStatusConflictException;

/**
 * Validator for order state transitions and access control rules.
 * Ensures orders follow the defined lifecycle and participants are authorized.
 */
@Component
public class OrderValidator {

	public void throwIfOrderStatusNotCancellable(OrderEntity order) {
		if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.COMPLETED) {
			throw new OrderStatusConflictException("Cannot cancel order in status:" + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotCompletable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.IN_PROGRESS) {
			throw new OrderStatusConflictException(
					"Only IN_PROGRESS orders can be completed. Current status: " + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotAcceptable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new OrderStatusConflictException(
					"Only PENDING orders can be accepted. Current status: " + order.getStatus());
		}
	}

	public void throwIfOrderStatusNotStartable(OrderEntity order) {
		if (order.getStatus() != OrderStatus.ACCEPTED) {
			throw new OrderStatusConflictException(
					"Only ACCEPTED orders can be started. Current status: " + order.getStatus());
		}
	}

	public void throwIfDriverNotAssignedToOrder(OrderEntity order, Long driverId) {
		if (order.getDriver() == null || !order.getDriver().getId().equals(driverId)) {
			throw new AccessDeniedException("Driver id=" + driverId + " is not assigned to this order.");
		}
	}

	public void throwIfClientNotAssignedToOrder(OrderEntity order, Long clientId) {
		if (order.getClient() == null || !order.getClient().getId().equals(clientId)) {
			throw new AccessDeniedException("Client id=" + clientId + " is not assigned to this order.");
		}
	}
}
