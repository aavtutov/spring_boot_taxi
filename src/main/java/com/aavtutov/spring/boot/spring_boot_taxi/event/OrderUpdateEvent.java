package com.aavtutov.spring.boot.spring_boot_taxi.event;

import java.time.Instant;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

public record OrderUpdateEvent(
		Long orderId,
		OrderStatus status,
		OrderCancellationSource cancellationSource,
		Instant occurredAt) {
}
