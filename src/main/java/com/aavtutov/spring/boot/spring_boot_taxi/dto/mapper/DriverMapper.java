package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DriverMapper {
	
	DriverResponseDTO toResponseDto(DriverEntity entity);
	
	// Note: Sensitive info is excluded
	DriverEntity fromResponseDto(DriverResponseDTO dto);
	
	// Note: ID and user-specific details must be 
    // populated by the service layer.
	DriverEntity fromCreateDto(DriverCreateDTO dto);
}
