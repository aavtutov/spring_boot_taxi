package com.aavtutov.spring.boot.spring_boot_taxi.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HibernateWarmupIndicator implements HealthIndicator {

	private final ClientRepository clientRepository;
	private final DriverRepository driverRepository;
	private final OrderRepository orderRepository;
	private final TelegramWebAppAuthValidator authValidator;

	@Override
	public Health health() {

		try {
			clientRepository.findById(-1L);
			driverRepository.findById(-1L);
			orderRepository.findById(-1L);
			
			authValidator.warmup();
			
			return Health.up().withDetail("warmup", "completed").build();
		} catch (Exception e) {
			log.error("Warmup failed!", e);
			return Health.down(e).build();
		}
	}
}
