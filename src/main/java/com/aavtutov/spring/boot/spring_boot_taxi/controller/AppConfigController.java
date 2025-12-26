package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class AppConfigController {
	
	@Value("${fare.base}")
    private double baseFare;
	
	@Value("${fare.perKm}")
    private double perKm;
	
	@Value("${fare.perMin}")
    private double perMin;
	
	@Value("${fare.currency}")
    private String currency;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> getConfig() {
		Map<String, Object> config = new HashMap<>();
		config.put("baseFare", baseFare);
		config.put("perKm", perKm);
		config.put("perMin", perMin);
		config.put("currency", currency);
		return ResponseEntity.ok(config);
	}

}
