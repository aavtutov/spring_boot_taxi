package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

@Component
public class OrderMapper {

	private final ClientMapper clientMapper;
	private final DriverMapper driverMapper;

	public OrderMapper(ClientMapper clientMapper, DriverMapper driverMapper) {
		this.clientMapper = clientMapper;
		this.driverMapper = driverMapper;
	}

	public OrderResponseDTO toResponseDto(OrderEntity entity) {

		OrderResponseDTO dto = new OrderResponseDTO();

		dto.setId(entity.getId());

		dto.setClient(clientMapper.toResponseDto(entity.getClient()));
		dto.setDriver(entity.getDriver() != null ? driverMapper.toResponseDto(entity.getDriver()) : null);

		dto.setStatus(entity.getStatus());

		dto.setStartAddress(entity.getStartAddress());
		dto.setEndAddress(entity.getEndAddress());

		dto.setStartLatitude(entity.getStartLatitude());
		dto.setStartLongitude(entity.getStartLongitude());
		dto.setEndLatitude(entity.getEndLatitude());
		dto.setEndLongitude(entity.getEndLongitude());

		dto.setAproximateDistance(entity.getAproximateDistance());
		dto.setAproximateDuration(entity.getAproximateDuration());
		dto.setActualDuration(entity.getActualDuration());

		dto.setPrice(entity.getPrice());
		dto.setBonusFare(entity.getBonusFare());

		dto.setCreatedAt(entity.getCreatedAt());
		dto.setAcceptedAt(entity.getAcceptedAt());
		dto.setStartedAt(entity.getStartedAt());
		dto.setCompletedAt(entity.getCompletedAt());
		dto.setCancelledAt(entity.getCancelledAt());

		dto.setCancellationSource(entity.getCancellationSource());

		dto.setTotalPrice(entity.getTotalPrice());

		dto.setNotes(entity.getNotes());

		return dto;
	}

	public OrderEntity fromResponseDto(OrderResponseDTO dto) {

		OrderEntity entity = new OrderEntity();

		entity.setId(dto.getId());

		entity.setClient(clientMapper.fromResponseDto(dto.getClient()));
		entity.setDriver(driverMapper.fromResponseDto(dto.getDriver()));

		entity.setStatus(dto.getStatus());

		entity.setStartAddress(dto.getStartAddress());
		entity.setEndAddress(dto.getEndAddress());

		entity.setStartLatitude(dto.getStartLatitude());
		entity.setStartLongitude(dto.getStartLongitude());
		entity.setEndLatitude(dto.getEndLatitude());
		entity.setEndLongitude(dto.getEndLongitude());

		entity.setAproximateDistance(dto.getAproximateDistance());
		entity.setAproximateDuration(dto.getAproximateDuration());
		entity.setActualDuration(dto.getActualDuration());

		entity.setPrice(dto.getPrice());
		entity.setBonusFare(dto.getBonusFare());

		entity.setCreatedAt(dto.getCreatedAt());
		entity.setAcceptedAt(dto.getAcceptedAt());
		entity.setStartedAt(dto.getStartedAt());
		entity.setCompletedAt(dto.getCompletedAt());
		entity.setCancelledAt(dto.getCancelledAt());

		entity.setCancellationSource(dto.getCancellationSource());

		entity.setTotalPrice(dto.getTotalPrice());

		entity.setNotes(dto.getNotes());

		return entity;
	}

	public OrderEntity fromCreateDto(OrderCreateDTO dto) {

		OrderEntity entity = new OrderEntity();

		entity.setStartAddress(dto.getStartAddress());
		entity.setEndAddress(dto.getEndAddress());

		entity.setStartLatitude(dto.getStartLatitude());
		entity.setStartLongitude(dto.getStartLongitude());
		entity.setEndLatitude(dto.getEndLatitude());
		entity.setEndLongitude(dto.getEndLongitude());

		entity.setPrice(dto.getPrice());
		entity.setBonusFare(dto.getBonusFare() != null ? dto.getBonusFare() : BigDecimal.ZERO);

		entity.setNotes(dto.getNotes() == "" ? null : dto.getNotes());

		return entity;
	}

}
