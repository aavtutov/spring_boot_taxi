package com.aavtutov.spring.boot.spring_boot_taxi.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.kafka.event.DriverLocationEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DriverLocationConsumer {

    @KafkaListener(topics = "driver-locations", groupId = "taxi-main-group")
    public void listen(DriverLocationEvent event) {
        log.info("==> [Kafka Consumer] Driver {} is now at [{}, {}]", 
                 event.driverId(), event.latitude(), event.longitude());
    }
}
