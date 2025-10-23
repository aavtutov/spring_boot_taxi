package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//Removed unused imports (DriverService, OrderService, OrderMapper, TelegramWebAppAuthValidator)

/**
 * Spring MVC Controller responsible for serving HTML views (Thymeleaf
 * templates) for the Telegram WebApp Mini client interface.
 *
 * <p>
 * Handles requests coming directly from the Telegram WebApp context and
 * prepares necessary data for the frontend.
 * </p>
 */
@Controller
public class ClientWebController {

	// Rationale: No instance fields are needed since this controller currently only
	// performs simple view rendering and model attribute setting.

	/**
	 * Constructs the ClientWebController. *
	 * <p>
	 * Note: Currently, no service dependencies are required for this controller's
	 * functionality.
	 * </p>
	 */
	public ClientWebController() {
		// Empty constructor
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
		// WebApp to authenticate the user and retrieve their specific data.
		model.addAttribute("initData", initData);

		// Return the name of the Thymeleaf view template
		return "client_order_history";
	}

}
