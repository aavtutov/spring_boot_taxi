package com.aavtutov.spring.boot.spring_boot_taxi.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.aavtutov.spring.boot.spring_boot_taxi.event.OrderUpdateEvent;
import com.aavtutov.spring.boot.spring_boot_taxi.kafka.event.OrderKafkaEvent;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KafkaEventMapper {
	
	OrderKafkaEvent toKafkaEvent(OrderUpdateEvent internalEvent);

}
