package com.aavtutov.spring.boot.spring_boot_taxi.kafka.producer;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.kafka.event.DriverLocationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLocationBridge {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener
    public void handleLocationUpdate(DriverLocationEvent event) {
        
        kafkaTemplate.send("driver-locations", String.valueOf(event.driverId()), event);
    }
}
