package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class ClientWebController {

	@GetMapping("/client/orders/history")
	public String getClientOrderHistoryPage() {
		return "client_order_history";
	}
}
