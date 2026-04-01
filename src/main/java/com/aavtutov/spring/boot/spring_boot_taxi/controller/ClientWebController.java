package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class ClientWebController {
	
	@Value("${mapbox.access.token}")
    private String mapboxAccessToken;
	
	@Value("${fare.calculation.strategy}")
	private String calculationStrategy;
	
	@Value("${fare.base}")
	private Double fareBase;
	
	@Value("${fare.perKm}")
	private Double farePerKm;
	
	@Value("${fare.perMin}")
	private Double farePerMin;
	
	@Value("${fare.currency}")
	private String fareCurrency;
	
	@GetMapping("/")
	public String index(Model model) {
	    model.addAttribute("mapboxAccessToken", mapboxAccessToken);
	    model.addAttribute("calculationStrategy", calculationStrategy);
	    model.addAttribute("fareBase", fareBase);
	    model.addAttribute("farePerKm", farePerKm);
	    model.addAttribute("farePerMin", farePerMin);
	    model.addAttribute("fareCurrency", fareCurrency);
	    return "index";
	}

	@GetMapping("/client/orders/history")
	public String getClientOrderHistoryPage() {
		return "client_order_history";
	}
}
