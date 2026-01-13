package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

/**
 * Mapper for converting between {@link DriverEntity} and driver-related DTOs.
 * Handles sensitive data filtering for public responses.
 */
@Component
public class DriverMapper {

	public DriverResponseDTO toResponseDto(DriverEntity entity) {
		if (entity == null) return null;

		DriverResponseDTO dto = new DriverResponseDTO();
		dto.setId(entity.getId());
		dto.setTelegramId(entity.getTelegramId());
		dto.setFullName(entity.getFullName());
		dto.setCarModel(entity.getCarModel());
		dto.setCarColor(entity.getCarColor());
		dto.setLicensePlate(entity.getLicensePlate());

		// Note: Sensitive document URLs and internal status are excluded from the response
		return dto;
	}

	public DriverEntity fromResponseDto(DriverResponseDTO dto) {
		if (dto == null) return null;

		DriverEntity entity = new DriverEntity();
		entity.setId(dto.getId());
		entity.setTelegramId(dto.getTelegramId());
		entity.setFullName(dto.getFullName());
		entity.setCarModel(dto.getCarModel());
		entity.setCarColor(dto.getCarColor());
		entity.setLicensePlate(dto.getLicensePlate());
		return entity;
	}

	public DriverEntity fromCreateDto(DriverCreateDTO dto) {
		if (dto == null) return null;

		DriverEntity entity = new DriverEntity();
		entity.setCarModel(dto.getCarModel());
		entity.setCarColor(dto.getCarColor());
		entity.setLicensePlate(dto.getLicensePlate());
		entity.setDriverLicenseUrl(dto.getDriverLicenseUrl());
		entity.setCarRegistrationUrl(dto.getCarRegistrationUrl());

		// Note: ID and user-specific details (Telegram ID, Status) must be 
        // populated by the service layer.
		return entity;
	}
}
