package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

public interface FareCalculator {

	BigDecimal calculateFare(OrderEntity order);

}
