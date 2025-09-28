package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.service.validator.OrderValidator;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final DriverRepository driverRepository;
	private final ClientRepository clientRepository;
	private final OrderValidator orderValidator;

	public OrderServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository,
			ClientRepository clientRepository, OrderValidator orderValidator) {
		this.orderRepository = orderRepository;
		this.driverRepository = driverRepository;
		this.clientRepository = clientRepository;
		this.orderValidator = orderValidator;
	}

	private OrderEntity findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with id=" + orderId + " not found"));
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	private ClientEntity findClientByIdOrThrow(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
	}

	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		ClientEntity client = findClientByIdOrThrow(clientId);
		order.setClient(client);
		return orderRepository.save(order);
	}

	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		// проверка что заказ существует
		findOrderByIdOrThrow(orderId);
		// пока что заказ показываем всем водителям, позже можно
		// TODO: добавить фильтрацию по критериям заказа
		return driverRepository.findByIsOnlineTrue();
	}

	@Override
	public OrderEntity acceptOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = findDriverByIdOrThrow(driverId);

		orderValidator.throwIfOrderStatusNotAcceptable(order);
		orderValidator.throwIfDriverOffline(driver);

		order.setDriver(driver);
		order.setStatus(OrderStatus.ACCEPTED);
		order.setAcceptedAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity startTrip(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotStartable(order);

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity completeOrder(Long orderId, Long driverId, BigDecimal updatedPrice) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCompletable(order);

		order.setStatus(OrderStatus.COMPLETED);
		order.setPrice(updatedPrice);
		order.setTotalPrice(updatedPrice.add(order.getBonusFare()));
		order.setCompletedAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity cancelOrderByDriver(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.DRIVER);
		order.setCancelledAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity cancelOrderByClient(Long orderId, Long clientId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfClientNotAssignedToOrder(order, clientId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.CLIENT);
		order.setCancelledAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity findOrderById(Long orderId) {
		return findOrderByIdOrThrow(orderId);
	}

}
