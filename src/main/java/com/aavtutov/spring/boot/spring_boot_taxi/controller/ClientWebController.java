package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

@Controller
public class ClientWebController {

	private final DriverService driverService;
	private final OrderService orderService;
	private final OrderMapper orderMapper;
	private final TelegramWebAppAuthValidator authValidator;

	public ClientWebController(DriverService driverService, OrderService orderService,
			TelegramWebAppAuthValidator authValidator, OrderMapper orderMapper) {
		this.driverService = driverService;
		this.orderService = orderService;
		this.authValidator = authValidator;
		this.orderMapper = orderMapper;
	}

	@GetMapping("/client/orders/history")
	public String getClientOrderHistoryPage(@RequestParam String initData, Model model) {
		model.addAttribute("initData", initData);
		return "client_order_history";
	}

}
