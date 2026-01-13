package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

import lombok.RequiredArgsConstructor;

/**
 * Mapper for converting between {@link OrderEntity} and order-related DTOs.
 * Handles nested mapping for Client and Driver entities.
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

	private final ClientMapper clientMapper;
	private final DriverMapper driverMapper;

	public OrderResponseDTO toResponseDto(OrderEntity entity) {
		if (entity == null) return null;

		OrderResponseDTO dto = new OrderResponseDTO();
		dto.setId(entity.getId());
		dto.setStatus(entity.getStatus());

		dto.setClient(clientMapper.toResponseDto(entity.getClient()));
		dto.setDriver(entity.getDriver() != null ? driverMapper.toResponseDto(entity.getDriver()) : null);

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
		dto.setTotalPrice(entity.getTotalPrice());
		dto.setNotes(entity.getNotes());

		dto.setCreatedAt(entity.getCreatedAt());
		dto.setAcceptedAt(entity.getAcceptedAt());
		dto.setStartedAt(entity.getStartedAt());
		dto.setCompletedAt(entity.getCompletedAt());
		dto.setCancelledAt(entity.getCancelledAt());
		dto.setCancellationSource(entity.getCancellationSource());

		return dto;
	}

	public OrderEntity fromResponseDto(OrderResponseDTO dto) {
		if (dto == null) return null;

		OrderEntity entity = new OrderEntity();
		entity.setId(dto.getId());
		entity.setStatus(dto.getStatus());

		entity.setClient(clientMapper.fromResponseDto(dto.getClient()));
		entity.setDriver(driverMapper.fromResponseDto(dto.getDriver()));

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
		entity.setTotalPrice(dto.getTotalPrice());
		entity.setNotes(dto.getNotes());

		entity.setCreatedAt(dto.getCreatedAt());
		entity.setAcceptedAt(dto.getAcceptedAt());
		entity.setStartedAt(dto.getStartedAt());
		entity.setCompletedAt(dto.getCompletedAt());
		entity.setCancelledAt(dto.getCancelledAt());
		entity.setCancellationSource(dto.getCancellationSource());

		return entity;
	}

	public OrderEntity fromCreateDto(OrderCreateDTO dto) {
		if (dto == null) return null;

		OrderEntity entity = new OrderEntity();
		entity.setStartAddress(dto.getStartAddress());
		entity.setEndAddress(dto.getEndAddress());
		entity.setStartLatitude(dto.getStartLatitude());
		entity.setStartLongitude(dto.getStartLongitude());
		entity.setEndLatitude(dto.getEndLatitude());
		entity.setEndLongitude(dto.getEndLongitude());
		entity.setPrice(dto.getPrice());

		// Rationale: If bonusFare is null in the DTO, initialize it to BigDecimal.ZERO
		// to ensure non-null integrity and correct calculations in the entity layer.
		entity.setBonusFare(Optional.ofNullable(dto.getBonusFare()).orElse(BigDecimal.ZERO));

		// Rationale: If notes is an empty string, set it to null for cleaner
		// persistence.
		entity.setNotes(dto.getNotes().isEmpty() ? null : dto.getNotes());

		return entity;
	}

}
