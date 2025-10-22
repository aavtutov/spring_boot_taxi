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

@Controller
public class DriverWebController {

	private final DriverService driverService;
	private final OrderService orderService;
	private final OrderMapper orderMapper;
	private final TelegramWebAppAuthValidator authValidator;

	public DriverWebController(DriverService driverService, OrderService orderService,
			TelegramWebAppAuthValidator authValidator, OrderMapper orderMapper) {
		this.driverService = driverService;
		this.orderService = orderService;
		this.authValidator = authValidator;
		this.orderMapper = orderMapper;
	}

	@GetMapping("/driver/dashboard")
	public String getDriverDashboard(@RequestParam String initData, Model model) {
		Long telegramId = authValidator.validate(initData);
		DriverEntity driver = driverService.findOptionalDriverByTelegramId(telegramId).orElse(null);

		if (driver == null) {
			return "driver_register_form";

		} else {
			model.addAttribute("driverStatus", driver.getStatus().name());

			if (driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
				return "driver_pending_approval";
			}

			return "driver_orders_list";
		}
	}

	@GetMapping("/driver/active-orders")
	public String getActiveOrdersPage(@RequestParam String initData, Model model) {
		Long telegramId = authValidator.validate(initData);
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);

		Optional<OrderEntity> activeOrder = orderService.findActiveOrderByDriver(driver.getId());

		if (activeOrder.isPresent()) {
			OrderResponseDTO orderDTO = orderMapper.toResponseDto(activeOrder.get());
			model.addAttribute("order", orderDTO);

		} else {
			model.addAttribute("noActiveOrder", true);
		}

		return "active_orders";
	}

}
