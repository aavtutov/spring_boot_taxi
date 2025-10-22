package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverOfflineException;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;
	private final ClientService clientService;
	private final DriverService driverService;
	private final OrderMapper orderMapper;
	private final DriverMapper driverMapper;
	private final TelegramWebAppAuthValidator authValidator;

	public OrderController(OrderService orderService, ClientService clientService, DriverService driverService,
			OrderMapper orderMapper, DriverMapper driverMapper,
			TelegramWebAppAuthValidator authValidator) {
		this.orderService = orderService;
		this.clientService = clientService;
		this.driverService = driverService;
		this.orderMapper = orderMapper;
		this.driverMapper = driverMapper;
		this.authValidator = authValidator;
	}

	@PostMapping
	public OrderResponseDTO placeOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO,
			@RequestHeader("X-Telegram-Init-Data") String initData) {

		Long telegramId = authValidator.validate(initData);
		ClientEntity client = clientService.findClientByTelegramId(telegramId);

		OrderEntity order = orderMapper.fromCreateDto(orderCreateDTO);
		OrderEntity savedOrder = orderService.placeOrder(order, client.getId());

		return orderMapper.toResponseDto(savedOrder);
	}

//	@PatchMapping("/{id}")
//	public OrderResponseDTO updateOrderStatus(
//			@PathVariable("id") Long orderId,
//			@RequestBody @Valid OrderUpdateDTO updateDTO,
//			@RequestHeader("X-Telegram-Init-Data") String initData) {
//
//		Long telegramId = authValidator.validate(initData);
//		Long clientId = clientService.findClientByTelegramId(telegramId).getId();
//
//		DriverEntity driver = null;
//
//		orderUpdateValidator.validate(updateDTO);
//		OrderAction action = updateDTO.getAction();
//		OrderEntity updatedOrder = null;
//
//		if (action.isDriverAction()) {
//
//			driver = driverService.findDriverByTelegramId(telegramId);
//			if (driver.getStatus() != DriverStatus.ACTIVE) {
//				throw new DriverOfflineException("Driver is not ACTIVE (current status: " + driver.getStatus() + ")");
//			}
//		}
//
//		switch (action) {
//
//		case ACCEPT -> {
//			updatedOrder = orderService.acceptOrder(orderId, driver.getId());
//		}
//
//		case START_TRIP -> {
//			updatedOrder = orderService.startTrip(orderId, driver.getId());
//		}
//
//		case COMPLETE -> {
//			updatedOrder = orderService.completeOrder(orderId, driver.getId(), updateDTO.getPrice());
//		}
//
//		case CANCEL_BY_DRIVER -> {
//			updatedOrder = orderService.cancelOrderByDriver(orderId, driver.getId());
//		}
//
//		case CANCEL_BY_CLIENT -> {
//			updatedOrder = orderService.cancelOrderByClient(orderId, clientId);
//		}
//
//		}
//
//		return orderMapper.toResponseDto(updatedOrder);
//	}

	@PatchMapping("/{id}")
	public OrderResponseDTO updateOrderStatus(@PathVariable("id") Long orderId,
			@RequestBody @Valid OrderUpdateDTO updateDTO, @RequestHeader("X-Telegram-Init-Data") String initData) {

		Long telegramId = authValidator.validate(initData);
		OrderEntity updatedOrder = null;

		switch (updateDTO.getAction()) {

		case ACCEPT, START_TRIP, COMPLETE, CANCEL_BY_DRIVER -> {
			DriverEntity driver = driverService.findDriverByTelegramId(telegramId);

			if (driver.getStatus() != DriverStatus.ACTIVE) {
				throw new DriverOfflineException("Driver is not ACTIVE (current status: " + driver.getStatus() + ")");
			}

			updatedOrder = switch (updateDTO.getAction()) {
			case ACCEPT -> orderService.acceptOrder(orderId, driver.getId());
			case START_TRIP -> orderService.startTrip(orderId, driver.getId());
			case COMPLETE -> orderService.completeOrder(orderId, driver.getId());
			case CANCEL_BY_DRIVER -> orderService.cancelOrderByDriver(orderId, driver.getId());
			default -> throw new IllegalStateException("Unexpected driver action");
			};
		}

		case CANCEL_BY_CLIENT -> {
			Long clientId = clientService.findClientByTelegramId(telegramId).getId();
			updatedOrder = orderService.cancelOrderByClient(orderId, clientId);
		}
		}

		return orderMapper.toResponseDto(updatedOrder);
	}

	@GetMapping("/{id}/find-suitable-drivers")
	public List<DriverResponseDTO> findSuitableDrivers(@PathVariable("id") Long orderId) {
		List<DriverEntity> suitableDrivers = orderService.findSuitableDrivers(orderId);
		return suitableDrivers.stream().map(driver -> driverMapper.toResponseDto(driver)).toList();
	}

	@GetMapping("/{id}")
	public OrderResponseDTO findOrderById(@PathVariable("id") Long orderId) {
		OrderEntity order = orderService.findOrderById(orderId);
		return orderMapper.toResponseDto(order);
	}

	@GetMapping
	public List<OrderResponseDTO> findAvailableOrders(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		driverService.findDriverByTelegramId(telegramId);

		List<OrderEntity> availableOrders = orderService.findAvailableOrders();
		return availableOrders.stream().map(orderMapper::toResponseDto).toList();
	}

	@GetMapping("/client-history")
	public List<OrderResponseDTO> getClientOrderHistory(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		Long clientId = clientService.findClientByTelegramId(telegramId).getId();
		List<OrderEntity> orders = orderService.findOrdersByClientId(clientId);
		return orders.stream().map(orderMapper::toResponseDto).toList();
	}

	@GetMapping("/client-current")
	public OrderResponseDTO getClientCurrentOrder(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		Long clientId = clientService.findClientByTelegramId(telegramId).getId();

		OrderEntity order = orderService.findCurrentOrderByClientId(clientId);
		return orderMapper.toResponseDto(order);
	}

}
