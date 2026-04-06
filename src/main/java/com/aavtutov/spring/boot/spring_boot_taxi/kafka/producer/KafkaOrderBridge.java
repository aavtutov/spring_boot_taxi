package com.aavtutov.spring.boot.spring_boot_taxi.kafka.producer;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.event.OrderUpdateEvent;
import com.aavtutov.spring.boot.spring_boot_taxi.kafka.event.OrderKafkaEvent;
import com.aavtutov.spring.boot.spring_boot_taxi.kafka.mapper.KafkaEventMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderBridge {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaEventMapper kafkaEventMapper;
	
	@EventListener
    public void handleOrderUpdate(OrderUpdateEvent internalEvent) {
        
		OrderKafkaEvent kafkaEvent = kafkaEventMapper.toKafkaEvent(internalEvent);

        log.info("==> [Kafka Bridge] Sending order {} to topic 'taxi-orders'", kafkaEvent.orderId());
        
        kafkaTemplate.send("taxi-orders", String.valueOf(kafkaEvent.orderId()), kafkaEvent);
    }
}
