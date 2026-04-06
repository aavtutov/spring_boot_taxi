package com.aavtutov.spring.boot.spring_boot_taxi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;

public record DriverLocationEvent(
		Long driverId,
		BigDecimal latitude,
		BigDecimal longitude,
		Double heading,
		Instant occurredAt) {}
