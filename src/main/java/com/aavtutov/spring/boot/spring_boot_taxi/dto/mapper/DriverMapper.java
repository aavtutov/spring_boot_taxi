package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

@Mapper
public interface DriverMapper {
	
	DriverResponseDTO toResponseDto(DriverEntity entity);
	
	// Note: Sensitive info is excluded
	@Mapping(target = "phoneNumber", ignore = true)
	@Mapping(target = "telegramChatId", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "driverLicenseUrl", ignore = true)
	@Mapping(target = "carRegistrationUrl", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	DriverEntity fromResponseDto(DriverResponseDTO dto);
	
	// Note: ID and user-specific details must be 
    // populated by the service layer.
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "fullName", ignore = true)
	@Mapping(target = "phoneNumber", ignore = true)
	@Mapping(target = "telegramId", ignore = true)
	@Mapping(target = "telegramChatId", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	DriverEntity fromCreateDto(DriverCreateDTO dto);
}
