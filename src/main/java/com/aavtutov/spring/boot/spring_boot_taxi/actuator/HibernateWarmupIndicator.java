package com.aavtutov.spring.boot.spring_boot_taxi.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HibernateWarmupIndicator implements HealthIndicator {

	private final ClientRepository clientRepository;
	private final DriverRepository driverRepository;
	private final OrderRepository orderRepository;

	@Override
	public Health health() {

		try {
			clientRepository.existsById(-1L);
			driverRepository.existsById(-1L);
			orderRepository.existsById(-1L);
			return Health.up().withDetail("hibernate", "warmed up").build();
		} catch (Exception e) {
			log.error("Hibernate warmup failed!", e);
			return Health.down(e).build();
		}
	}
}
