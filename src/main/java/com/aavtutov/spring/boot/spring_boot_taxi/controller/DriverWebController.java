package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

/**
 * Spring MVC Controller dedicated to serving HTML views (Thymeleaf templates)
 * for the Driver's Telegram WebApp Mini interface.
 *
 * <p>
 * Handles the core driver workflow: dashboard access, registration checks, and
 * active order display.
 * </p>
 */
@Controller
public class DriverWebController {

	private final DriverService driverService;
	private final OrderService orderService;
	private final OrderMapper orderMapper;
	private final TelegramWebAppAuthValidator authValidator;

	/**
	 * Constructs the controller, injecting required services and the WebApp
	 * validator component.
	 *
	 * @param driverService Service layer for driver business logic.
	 * @param orderService  Service layer for order business logic.
	 * @param authValidator Component to validate Telegram WebApp authentication
	 *                      data.
	 * @param orderMapper   Utility for converting order entities to DTOs.
	 */
	public DriverWebController(DriverService driverService, OrderService orderService,
			TelegramWebAppAuthValidator authValidator, OrderMapper orderMapper) {
		this.driverService = driverService;
		this.orderService = orderService;
		this.authValidator = authValidator;
		this.orderMapper = orderMapper;
	}

	/**
	 * Determines and serves the appropriate driver dashboard view based on the
	 * driver's registration and approval status.
	 *
	 * <p>
	 * Endpoint: GET /driver/dashboard
	 * </p>
	 *
	 * @param initData The authentication and user data provided by the Telegram
	 *                 WebApp client.
	 * @param model    The Spring Model for passing data to the Thymeleaf template.
	 * @return The name of the specific Thymeleaf template (registration form,
	 *         pending approval, or orders list).
	 */
	@GetMapping("/driver/dashboard")
	public String getDriverDashboard(@RequestParam String initData, Model model) {

		// 1. Validate the WebApp user identity and retrieve Telegram ID.
		Long telegramId = authValidator.validate(initData);

		// 2. Check if a driver profile exists for this Telegram user.
		DriverEntity driver = driverService.findOptionalDriverByTelegramId(telegramId).orElse(null);

		if (driver == null) {

			// Rationale: If no driver profile exists, redirect to the registration form.
			return "driver_register_form";

		} else {

			// Rationale: Pass the current status to the frontend for UI logic (e.g.,
			// enable/disable online button).
			model.addAttribute("driverStatus", driver.getStatus().name());

			if (driver.getStatus() == DriverStatus.PENDING_APPROVAL) {

				// Driver is registered but awaiting administrative review.
				return "driver_pending_approval";
			}

			// Driver is active/offline, show the main operational dashboard.
			return "driver_orders_list";
		}
	}

	/**
	 * Serves the page displaying the driver's currently active order, if one
	 * exists.
	 *
	 * <p>
	 * Endpoint: GET /driver/active-orders
	 * </p>
	 *
	 * @param initData The authentication and user data provided by the Telegram
	 *                 WebApp client.
	 * @param model    The Spring Model for passing data to the Thymeleaf template.
	 * @return The name of the Thymeleaf template ("active_orders").
	 */
	@GetMapping("/driver/active-order")
	public String getActiveOrdersPage(@RequestParam String initData, Model model) {
		Long telegramId = authValidator.validate(initData);
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);

		// Find if the driver is currently assigned and active in any order.
		Optional<OrderEntity> activeOrder = orderService.findActiveOrderByDriver(driver.getId());

		if (activeOrder.isPresent()) {

			// Add order details to the model for display.
			OrderResponseDTO orderDTO = orderMapper.toResponseDto(activeOrder.get());
			model.addAttribute("order", orderDTO);

		} else {

			// Signal to the frontend that there is no active order to display.
			model.addAttribute("noActiveOrder", true);
		}

		return "driver_active_order";
	}

}
