package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

@Mapper(uses = {ClientMapper.class, DriverMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
	
	OrderResponseDTO toResponseDto(OrderEntity entity);
	
	OrderEntity fromResponseDto(OrderResponseDTO dto);
	
	@Mapping(target = "bonusFare", source = "bonusFare", defaultValue = "0")
	OrderEntity fromCreateDto(OrderCreateDTO dto);
	
	default String mapEmptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value;
    }
}
