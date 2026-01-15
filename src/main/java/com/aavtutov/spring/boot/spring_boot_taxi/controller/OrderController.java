package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverOfflineException;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing customer orders.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final ClientService clientService;
	private final DriverService driverService;
	private final OrderMapper orderMapper;

	@PostMapping
	public OrderResponseDTO placeOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO,
			ClientEntity client) {
		OrderEntity order = orderMapper.fromCreateDto(orderCreateDTO);
		OrderEntity savedOrder = orderService.placeOrder(order, client.getId());
		return orderMapper.toResponseDto(savedOrder);
	}

	@PatchMapping("/{id}")
	public OrderResponseDTO updateOrderStatus(@PathVariable("id") Long orderId,
			@RequestBody @Valid OrderUpdateDTO updateDTO,
			TelegramUserDTO tgUser) {

		OrderEntity updatedOrder = null;

		switch (updateDTO.getAction()) {
			
			// Driver Actions
			case ACCEPT, START_TRIP, COMPLETE, CANCEL_BY_DRIVER -> {
				DriverEntity driver = driverService.findDriverByTelegramId(tgUser.getId());
				
				if (driver.getStatus() != DriverStatus.ACTIVE) {
					// Driver must be ACTIVE to accept/start/complete an order.
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
			
			// Client Actions
			case CANCEL_BY_CLIENT -> {
				Long clientId = clientService.findClientByTelegramId(tgUser.getId()).getId();
				updatedOrder = orderService.cancelOrderByClient(orderId, clientId);
			}
			
		}
		return orderMapper.toResponseDto(updatedOrder);
	}


	@GetMapping
	public List<OrderResponseDTO> findAvailableOrders(DriverEntity driver) {
		List<OrderEntity> availableOrders = orderService.findAvailableOrders();
		return availableOrders.stream().map(orderMapper::toResponseDto).toList();
	}
	
	@GetMapping("/current")
	public OrderResponseDTO getClientCurrentOrder(ClientEntity client) {
		OrderEntity order = orderService.findMostRecentOrderByClientId(client.getId());
		return orderMapper.toResponseDto(order);
	}

	@GetMapping("/client-history")
	public List<OrderResponseDTO> getClientOrderHistory(ClientEntity client) {
		List<OrderEntity> orders = orderService.findOrdersByClientId(client.getId());
		return orders.stream().map(orderMapper::toResponseDto).toList();
	}

	@GetMapping("/driver-history")
	public List<OrderResponseDTO> getDriverOrderHistory(DriverEntity driver) {
		List<OrderEntity> orders = orderService.findOrdersByDriverId(driver.getId());
		return orders.stream().map(orderMapper::toResponseDto).toList();
	}
	
	/**
     * NOTE: In a production environment, you need verify that the authenticated 
     * user (TelegramUserDTO) is either the client or the driver of this order.
     */
	@GetMapping("/{id}")
	public OrderResponseDTO findOrderById(@PathVariable("id") Long orderId, TelegramUserDTO tgUser) {
		OrderEntity order = orderService.findOrderById(orderId);
		return orderMapper.toResponseDto(order);
	}

}
