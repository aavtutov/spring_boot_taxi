package com.aavtutov.spring.boot.spring_boot_taxi.kafka.consumer;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.kafka.event.DriverLocationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverLocationConsumer {
	
	private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "driver-locations", groupId = "taxi-main-group")
    public void listen(DriverLocationEvent event) {
    	
    	Long orderId = event.orderId();
    	String topic = "/topic/order-location/" + orderId;
    	
    	Map<String, BigDecimal> coordinates = Map.of(
    	        "lat", event.latitude(),
    	        "lon", event.longitude()
    	    );
    	
    	simpMessagingTemplate.convertAndSend(topic, coordinates);
    }
}
