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

/**
 * REST Controller for managing all taxi order lifecycle operations.
 *
 * <p>
 * Handles order creation by clients and status updates initiated by both
 * clients and drivers. All user-initiated actions require Telegram WebApp
 * authentication.
 * </p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;
	private final ClientService clientService;
	private final DriverService driverService;
	private final OrderMapper orderMapper;
	private final DriverMapper driverMapper;
	private final TelegramWebAppAuthValidator authValidator;

	/**
	 * Constructs the OrderController, injecting all necessary services, mappers,
	 * and the security validator.
	 */
	public OrderController(OrderService orderService, ClientService clientService, DriverService driverService,
			OrderMapper orderMapper, DriverMapper driverMapper, TelegramWebAppAuthValidator authValidator) {
		this.orderService = orderService;
		this.clientService = clientService;
		this.driverService = driverService;
		this.orderMapper = orderMapper;
		this.driverMapper = driverMapper;
		this.authValidator = authValidator;
	}

	/**
	 * Places a new taxi order initiated by a client.
	 *
	 * <p>
	 * Endpoint: POST /api/orders
	 * </p>
	 *
	 * @param orderCreateDTO The request body containing the pickup/drop-off
	 *                       details.
	 * @param initData       The authentication header from the Telegram WebApp
	 *                       (client).
	 * @return The newly created order's details.
	 */
	@PostMapping
	public OrderResponseDTO placeOrder(@RequestBody @Valid OrderCreateDTO orderCreateDTO,
			@RequestHeader("X-Telegram-Init-Data") String initData) {

		// Authenticate client and retrieve ClientEntity for ID.
		Long telegramId = authValidator.validate(initData);
		ClientEntity client = clientService.findClientByTelegramId(telegramId);

		OrderEntity order = orderMapper.fromCreateDto(orderCreateDTO);
		OrderEntity savedOrder = orderService.placeOrder(order, client.getId());

		return orderMapper.toResponseDto(savedOrder);
	}

	/**
	 * Updates the status of an existing order based on the action (e.g., ACCEPT,
	 * COMPLETE, CANCEL).
	 *
	 * <p>
	 * This complex endpoint handles actions initiated by both drivers and clients.
	 * </p>
	 * <p>
	 * Endpoint: PATCH /api/orders/{id}
	 * </p>
	 *
	 * @param orderId   The ID of the order to update.
	 * @param updateDTO The request body containing the desired action (e.g.,
	 *                  ACCEPT, CANCEL_BY_CLIENT).
	 * @param initData  The authentication header from the Telegram WebApp (client
	 *                  or driver).
	 * @return The updated order's details.
	 * @throws DriverOfflineException If a driver-initiated action is attempted
	 *                                while the driver is not ACTIVE.
	 */
	@PatchMapping("/{id}")
	public OrderResponseDTO updateOrderStatus(@PathVariable("id") Long orderId,
			@RequestBody @Valid OrderUpdateDTO updateDTO, @RequestHeader("X-Telegram-Init-Data") String initData) {

		Long telegramId = authValidator.validate(initData);
		OrderEntity updatedOrder = null;

		// Rationale: Use a switch statement on the action type to cleanly separate
		// driver vs. client logic.
		switch (updateDTO.getAction()) {

		// --- Driver Actions ---
		case ACCEPT, START_TRIP, COMPLETE, CANCEL_BY_DRIVER -> {

			// 1. Retrieve and validate the driver initiating the action.
			DriverEntity driver = driverService.findDriverByTelegramId(telegramId);

			if (driver.getStatus() != DriverStatus.ACTIVE) {

				// Critical security/business check: A driver must be ACTIVE to
				// accept/start/complete an order.
				throw new DriverOfflineException("Driver is not ACTIVE (current status: " + driver.getStatus() + ")");
			}

			// 2. Delegate to the appropriate service method based on the action.
			updatedOrder = switch (updateDTO.getAction()) {
			case ACCEPT -> orderService.acceptOrder(orderId, driver.getId());
			case START_TRIP -> orderService.startTrip(orderId, driver.getId());
			case COMPLETE -> orderService.completeOrder(orderId, driver.getId());
			case CANCEL_BY_DRIVER -> orderService.cancelOrderByDriver(orderId, driver.getId());

			// Should be unreachable due to the outer switch, but included for compiler
			// safety/robustness.
			default -> throw new IllegalStateException("Unexpected driver action");
			};
		}

		// --- Client Actions ---
		case CANCEL_BY_CLIENT -> {

			// 1. Retrieve client ID for authorization check in the service layer.
			Long clientId = clientService.findClientByTelegramId(telegramId).getId();

			// 2. Execute client-specific action.
			updatedOrder = orderService.cancelOrderByClient(orderId, clientId);
		}
		}

		return orderMapper.toResponseDto(updatedOrder);
	}

	// The commented-out code is removed for clarity, as the final version is
	// implemented above.
	// The previous commented block served as a good historical reference during
	// refactoring,
	// but should be deleted or moved to version control (Git) in the final code
	// base.

	/**
	 * Finds a list of drivers deemed suitable (e.g., available, nearby) for a
	 * specific order.
	 *
	 * <p>
	 * This is typically used by the dispatch service or a client to visualize
	 * options.
	 * </p>
	 * <p>
	 * Endpoint: GET /api/orders/{id}/find-suitable-drivers
	 * </p>
	 *
	 * @param orderId The ID of the order to find drivers for.
	 * @return A list of matching {@link DriverResponseDTO}s.
	 */
	@GetMapping("/{id}/find-suitable-drivers")
	public List<DriverResponseDTO> findSuitableDrivers(@PathVariable("id") Long orderId) {
		List<DriverEntity> suitableDrivers = orderService.findSuitableDrivers(orderId);
		return suitableDrivers.stream().map(driver -> driverMapper.toResponseDto(driver)).toList();
	}

	/**
	 * Retrieves the full details of a specific order by its ID.
	 *
	 * <p>
	 * Endpoint: GET /api/orders/{id}
	 * </p>
	 *
	 * @param orderId The primary key ID of the order.
	 * @return The order details as a response DTO.
	 */
	@GetMapping("/{id}")
	public OrderResponseDTO findOrderById(@PathVariable("id") Long orderId) {
		OrderEntity order = orderService.findOrderById(orderId);
		return orderMapper.toResponseDto(order);
	}

	/**
	 * Retrieves a list of orders currently available for assignment to a driver.
	 *
	 * <p>
	 * This endpoint validates that the requesting user is a registered driver.
	 * </p>
	 * <p>
	 * Endpoint: GET /api/orders
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp (driver).
	 * @return A list of available orders.
	 */
	@GetMapping
	public List<OrderResponseDTO> findAvailableOrders(@RequestHeader("X-Telegram-Init-Data") String initData) {

		// Authenticate and check driver existence before fetching available orders.
		Long telegramId = authValidator.validate(initData);
		driverService.findDriverByTelegramId(telegramId);

		List<OrderEntity> availableOrders = orderService.findAvailableOrders();
		return availableOrders.stream().map(orderMapper::toResponseDto).toList();
	}

	/**
	 * Retrieves the order history for the authenticated client.
	 *
	 * <p>
	 * Endpoint: GET /api/orders/client-history
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp (client).
	 * @return A list of the client's past orders.
	 */
	@GetMapping("/client-history")
	public List<OrderResponseDTO> getClientOrderHistory(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		Long clientId = clientService.findClientByTelegramId(telegramId).getId();
		List<OrderEntity> orders = orderService.findOrdersByClientId(clientId);
		return orders.stream().map(orderMapper::toResponseDto).toList();
	}

	/**
	 * Retrieves the single active order for the authenticated client, if one
	 * exists.
	 *
	 * <p>
	 * Endpoint: GET /api/orders/client-current
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp (client).
	 * @return The current order's details.
	 */
	@GetMapping("/client-current")
	public OrderResponseDTO getClientCurrentOrder(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		Long clientId = clientService.findClientByTelegramId(telegramId).getId();

		OrderEntity order = orderService.findCurrentOrderByClientId(clientId);
		return orderMapper.toResponseDto(order);
	}

	/**
	 * Retrieves the order history for the authenticated driver.
	 *
	 * <p>
	 * Endpoint: GET /api/orders/driver-history
	 * </p>
	 *
	 * @param initData The authentication header from the Telegram WebApp (driver).
	 * @return A list of the driver's past orders.
	 */
	@GetMapping("/driver-history")
	public List<OrderResponseDTO> getDriverOrderHistory(@RequestHeader("X-Telegram-Init-Data") String initData) {
		Long telegramId = authValidator.validate(initData);
		Long driverId = driverService.findDriverByTelegramId(telegramId).getId();
		List<OrderEntity> orders = orderService.findOrdersByDriverId(driverId);
		return orders.stream().map(orderMapper::toResponseDto).toList();
	}
	
	
}
