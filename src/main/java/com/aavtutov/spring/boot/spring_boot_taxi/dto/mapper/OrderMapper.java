package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

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

	public OrderResponseDTO toResponseDto(OrderEntity order) {

		OrderResponseDTO dto = new OrderResponseDTO();

		dto.setId(order.getId());

		dto.setClient(clientMapper.toResponseDto(order.getClient()));
		dto.setDriver(order.getDriver() != null ? driverMapper.toResponseDto(order.getDriver()) : null);

		dto.setStatus(order.getStatus());

		dto.setStartAddress(order.getStartAddress());
		dto.setEndAddress(order.getEndAddress());

		dto.setStartLatitude(order.getStartLatitude());
		dto.setStartLongitude(order.getStartLongitude());
		dto.setEndLatitude(order.getEndLatitude());
		dto.setEndLongitude(order.getEndLongitude());

		dto.setPrice(order.getPrice());
		dto.setBonusFare(order.getBonusFare());

		dto.setMapScreenshotUrl(order.getMapScreenshotUrl());
		dto.setLocationPhotoUrl(order.getLocationPhotoUrl());

		dto.setCreatedAt(order.getCreatedAt());
		dto.setAcceptedAt(order.getAcceptedAt());
		dto.setStartedAt(order.getStartedAt());
		dto.setCompletedAt(order.getCompletedAt());
		dto.setCancelledAt(order.getCancelledAt());

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

		entity.setPrice(dto.getPrice());
		entity.setBonusFare(dto.getBonusFare());

		entity.setMapScreenshotUrl(dto.getMapScreenshotUrl());
		entity.setLocationPhotoUrl(dto.getLocationPhotoUrl());

		entity.setCreatedAt(dto.getCreatedAt());
		entity.setAcceptedAt(dto.getAcceptedAt());
		entity.setStartedAt(dto.getStartedAt());
		entity.setCompletedAt(dto.getCompletedAt());
		entity.setCancelledAt(dto.getCancelledAt());

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
		entity.setBonusFare(dto.getBonusFare());

		entity.setMapScreenshotUrl(dto.getMapScreenshotUrl());
		entity.setLocationPhotoUrl(dto.getLocationPhotoUrl());

		return entity;
	}

}
