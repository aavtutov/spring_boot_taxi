package com.aavtutov.spring.boot.spring_boot_taxi.service.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.AccessDeniedException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ActiveOrderAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverOfflineException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderStatusConflictException;

import lombok.RequiredArgsConstructor;

/**
 * Validator for order state transitions and access control rules.
 * Ensures orders follow the defined lifecycle and participants are authorized.
 */
@Component
@RequiredArgsConstructor
public class OrderValidator {
	
	private final OrderRepository orderRepository;
	private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED,
			OrderStatus.IN_PROGRESS);

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
	
	public void throwIfDriverNotActive(DriverEntity driver) {
		if (driver.getStatus() != DriverStatus.ACTIVE) {
			throw new DriverOfflineException("Driver is not ACTIVE (current status: " + driver.getStatus() + ")");
		}
	}
	
	public void throwIfDriverHasActiveOrder(Long driverId) {
        if (orderRepository.existsByDriverIdAndStatusIn(driverId, ACTIVE_ORDER_STATUSES)) {
            throw new ActiveOrderAlreadyExistsException("Driver id=" + driverId + " already has an active order.");
        }
    }
	
	public void throwIfClientHasActiveOrder(Long clientId) {
		if (orderRepository.existsByClientIdAndStatusIn(clientId, ACTIVE_ORDER_STATUSES)) {
			throw new ActiveOrderAlreadyExistsException("Client id=" + clientId + " already has an active order.");
		}
	}
}
