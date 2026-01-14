package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    ClientResponseDTO toResponseDto(ClientEntity entity);

    ClientEntity fromResponseDto(ClientResponseDTO dto);

    ClientEntity fromCreateDto(ClientCreateDTO dto);
    
    @Mapping(target = "telegramId", source = "id")
    @Mapping(target = "fullName", source = "firstName")
    @Mapping(target = "telegramChatId", expression = "java(String.valueOf(dto.getId()))")
    ClientEntity toEntity(TelegramUserDTO dto);
}
