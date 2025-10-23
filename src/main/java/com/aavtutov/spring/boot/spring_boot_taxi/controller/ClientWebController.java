package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

/**
 * Spring MVC Controller responsible for serving HTML views (Thymeleaf
 * templates) for the Telegram WebApp Mini client interface.
 *
 * <p>
 * Handles requests coming directly from the Telegram WebApp context.
 * </p>
 */
@Controller
public class ClientWebController {

	private final DriverService driverService;
	private final OrderService orderService;
	private final OrderMapper orderMapper;
	private final TelegramWebAppAuthValidator authValidator;

	/**
	 * Constructs the controller, injecting necessary services and the WebApp
	 * validator.
	 *
	 * @param driverService Service for driver-related business logic.
	 * @param orderService  Service for order-related business logic.
	 * @param authValidator Component to validate Telegram WebApp authentication
	 *                      data.
	 * @param orderMapper   Utility for converting order entities to DTOs.
	 */
	public ClientWebController(DriverService driverService, OrderService orderService,
			TelegramWebAppAuthValidator authValidator, OrderMapper orderMapper) {
		this.driverService = driverService;
		this.orderService = orderService;
		this.authValidator = authValidator;
		this.orderMapper = orderMapper;
	}

	/**
	 * Serves the client's order history page, typically accessed via the Telegram
	 * WebApp.
	 *
	 * <p>
	 * Endpoint: GET /client/orders/history
	 * </p>
	 *
	 * @param initData The authentication and user data provided by the Telegram
	 *                 WebApp client. // TODO: [SECURITY] Implement explicit
	 *                 validation of initData here or within a Filter/Interceptor //
	 *                 before processing the request to ensure it is authentic and
	 *                 hasn't been tampered with.
	 * @param model    The Spring Model for passing data to the Thymeleaf template.
	 * @return The name of the Thymeleaf template ("client_order_history").
	 */
	@GetMapping("/client/orders/history")
	public String getClientOrderHistoryPage(@RequestParam String initData, Model model) {

		// Rationale: The initData is crucial for subsequent API calls made by the
		// WebApp
		// to authenticate the user and retrieve their specific data.
		model.addAttribute("initData", initData);

		// Return the name of the Thymeleaf view template
		return "client_order_history";
	}

}
