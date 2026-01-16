package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverWebController {

	private final DriverService driverService;
	private final OrderService orderService;
	private final OrderMapper orderMapper;

	/**
     * Entry point for the driver section. 
     * Determines whether to show the registration form, pending screen, or the dashboard.
     */
	@GetMapping("/dashboard")
	public String getDriverDashboard(ClientEntity client, Model model) {
		return driverService.findByTelegramId(client.getTelegramId())
		.map(driver -> {
			model.addAttribute("driverStatus", driver.getStatus().name());
			if (driver.getStatus() == DriverStatus.PENDING_APPROVAL) {
                return "driver_pending_approval";
            }
			return "driver_orders_list";
		})
		.orElse("driver_register_form");
	}

	/**
     * Displays the current active order assigned to the driver.
     */
	@GetMapping("/active-order")
	public String getActiveOrdersPage(DriverEntity driver, Model model) {
		orderService.findActiveOrderByDriver(driver.getId()).ifPresentOrElse(
				order -> model.addAttribute("order", orderMapper.toResponseDto(order)),
				() -> model.addAttribute("noActiveOrder", true));
		return "driver_active_order";
	}
	
	@GetMapping("/orders-history")
	public String getClientOrderHistoryPage() {
		return "driver_orders_history";
	}
}
