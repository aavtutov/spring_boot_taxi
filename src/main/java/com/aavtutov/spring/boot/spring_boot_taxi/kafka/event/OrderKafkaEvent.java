package com.aavtutov.spring.boot.spring_boot_taxi.kafka.event;

import java.time.Instant;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

public record OrderKafkaEvent(
		Long orderId,
		Long driverTelegramId,
		OrderStatus status,
		OrderCancellationSource cancellationSource,
		Instant occurredAt) {
}
