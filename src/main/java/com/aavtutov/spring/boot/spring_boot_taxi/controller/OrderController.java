package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderAction;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
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

	public OrderController(OrderService orderService, OrderMapper orderMapper, DriverMapper driverMapper,
			OrderUpdateValidator orderUpdateValidator) {
		this.orderService = orderService;
		this.orderMapper = orderMapper;
		this.driverMapper = driverMapper;
		this.orderUpdateValidator = orderUpdateValidator;
	}

	private OrderResponseDTO toResponseDto(OrderEntity orderEntity) {
		return orderMapper.toResponseDto(orderEntity);
	}

	@PostMapping
	public OrderResponseDTO placeOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO,
			@RequestParam("client-id") Long clientId) {
		OrderEntity order = orderMapper.fromCreateDto(orderCreateDTO);
		OrderEntity savedOrder = orderService.placeOrder(order, clientId);
		return toResponseDto(savedOrder);
	}

//	@PatchMapping("/{id}/accept")
//	public OrderResponseDTO acceptOrder(@PathVariable("id") Long orderId, @RequestParam("driver-id") Long driverId) {
//		OrderEntity updatedOrder = orderService.acceptOrder(orderId, driverId);
//		return toDto(updatedOrder);
//	}
//
//	@PatchMapping("/{id}/complete")
//	public OrderResponseDTO completeOrderByDriver(@PathVariable("id") Long orderId,
//			@RequestParam("driver-id") Long driverId, @RequestParam("price") BigDecimal finalPrice) {
//		OrderEntity updatedOrder = orderService.completeOrder(orderId, driverId, finalPrice);
//		return toDto(updatedOrder);
//	}
//
//	@PatchMapping("/{id}/cancel-by-driver")
//	public OrderResponseDTO cancelOrderByDriver(@PathVariable("id") Long orderId,
//			@RequestParam("driver-id") Long driverId) {
//		OrderEntity updatedOrder = orderService.cancelOrderByDriver(orderId, driverId);
//		return toDto(updatedOrder);
//	}
//
//	@PatchMapping("/{id}/cancel-by-client")
//	public OrderResponseDTO cancelOrderByClient(@PathVariable("id") Long orderId,
//			@RequestParam("client-id") Long clientId) {
//		OrderEntity updatedOrder = orderService.cancelOrderByClient(orderId, clientId);
//		return toDto(updatedOrder);
//	}

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
