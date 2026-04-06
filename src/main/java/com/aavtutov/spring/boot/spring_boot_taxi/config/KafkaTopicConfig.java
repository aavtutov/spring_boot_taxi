package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
	
	@Bean
	NewTopic taxiOrdersTopic() {
		return TopicBuilder.name("taxi-orders")
				.partitions(3)
				.replicas(3)
				.build();
	}
	
	@Bean
	NewTopic driverLocationsTopic() {
		return TopicBuilder.name("driver-locations")
				.partitions(6)
				.replicas(3)
				.build();
	}
}
