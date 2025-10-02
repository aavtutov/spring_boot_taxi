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
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderAction;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.validator.OrderUpdateValidator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;
	private final OrderMapper orderMapper;
	private final DriverMapper driverMapper;
	private final OrderUpdateValidator orderUpdateValidator;
	private final ClientService clientService;
	private final TelegramWebAppAuthValidator authValidator;

	public OrderController(OrderService orderService, OrderMapper orderMapper, DriverMapper driverMapper,
			OrderUpdateValidator orderUpdateValidator, ClientService clientService,
			TelegramWebAppAuthValidator authValidator) {
		this.orderService = orderService;
		this.orderMapper = orderMapper;
		this.driverMapper = driverMapper;
		this.orderUpdateValidator = orderUpdateValidator;
		this.clientService = clientService;
		this.authValidator = authValidator;
	}

	private OrderResponseDTO toResponseDto(OrderEntity orderEntity) {
		return orderMapper.toResponseDto(orderEntity);
	}

	@PostMapping
	public OrderResponseDTO placeOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO,
			@RequestHeader("X-Telegram-Init-Data") String initData) {
		
		// validate initData and get telegramId
		Long telegramId = authValidator.validate(initData);
		
		// find client by telegramId
		ClientEntity client = clientService.findClientByTelegramId(telegramId);
		
		OrderEntity order = orderMapper.fromCreateDto(orderCreateDTO);
		OrderEntity savedOrder = orderService.placeOrder(order, client.getId());
		
		return toResponseDto(savedOrder);
	}

	@PatchMapping("/{id}")
	public OrderResponseDTO updateOrderStatus(@PathVariable("id") Long orderId,
			@RequestBody @Valid OrderUpdateDTO updateDTO) {

		orderUpdateValidator.validate(updateDTO);

		OrderEntity updatedOrder = null;
		OrderAction action = updateDTO.getAction();

		switch (action) {

		case ACCEPT -> {
			updatedOrder = orderService.acceptOrder(orderId, updateDTO.getDriverId());
		}

		case START_TRIP -> {
			updatedOrder = orderService.startTrip(orderId, updateDTO.getDriverId());
		}

		case COMPLETE -> {
			updatedOrder = orderService.completeOrder(orderId, updateDTO.getDriverId(), updateDTO.getPrice());
		}

		case CANCEL_BY_DRIVER -> {
			updatedOrder = orderService.cancelOrderByDriver(orderId, updateDTO.getDriverId());
		}

		case CANCEL_BY_CLIENT -> {
			updatedOrder = orderService.cancelOrderByClient(orderId, updateDTO.getClientId());
		}

		}

		return toResponseDto(updatedOrder);
	}

	@GetMapping("/{id}/find-suitable-drivers")
	public List<DriverResponseDTO> findSuitableDrivers(@PathVariable("id") Long orderId) {
		List<DriverEntity> suitableDrivers = orderService.findSuitableDrivers(orderId);
		return suitableDrivers.stream().map(driver -> driverMapper.toResponseDto(driver)).toList();
	}

	@GetMapping("/{id}")
	public OrderResponseDTO findOrderById(@PathVariable("id") Long orderId) {
		OrderEntity order = orderService.findOrderById(orderId);
		return toResponseDto(order);
	}

}
