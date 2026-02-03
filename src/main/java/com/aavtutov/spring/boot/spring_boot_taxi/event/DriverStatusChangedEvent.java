package com.aavtutov.spring.boot.spring_boot_taxi.event;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

public record DriverStatusChangedEvent(DriverEntity driver) {
}
