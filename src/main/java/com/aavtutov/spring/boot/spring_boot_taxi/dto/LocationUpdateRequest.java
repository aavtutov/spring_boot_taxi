package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LocationUpdateRequest(
		BigDecimal latitude,
	    BigDecimal longitude,
	    Double heading,
	    Instant occurredAt) {}
