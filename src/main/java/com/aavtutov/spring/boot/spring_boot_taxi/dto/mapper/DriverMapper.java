package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

@Component
public class DriverMapper {

	public DriverResponseDTO toResponseDto(DriverEntity driver) {

		DriverResponseDTO dto = new DriverResponseDTO();
		dto.setId(driver.getId());
		dto.setTelegramId(driver.getTelegramId());
		dto.setFullName(driver.getFullName());
		dto.setCarModel(driver.getCarModel());
		dto.setLicensePlate(driver.getLicensePlate());

		return dto;
	}

	public DriverEntity fromResponseDto(DriverResponseDTO dto) {

		DriverEntity entity = new DriverEntity();
		entity.setId(dto.getId());
		entity.setTelegramId(dto.getTelegramId());
		entity.setFullName(dto.getFullName());
		entity.setCarModel(dto.getCarModel());
		entity.setLicensePlate(dto.getLicensePlate());

		return entity;
	}

	public DriverEntity fromCreateDto(DriverCreateDTO dto) {

		DriverEntity entity = new DriverEntity();
		entity.setTelegramId(dto.getTelegramId());
		entity.setPhoneNumber(dto.getPhoneNumber());
		entity.setFullName(dto.getFullName());
		entity.setCarModel(dto.getCarModel());
		entity.setLicensePlate(dto.getLicensePlate());

		return entity;
	}
}
