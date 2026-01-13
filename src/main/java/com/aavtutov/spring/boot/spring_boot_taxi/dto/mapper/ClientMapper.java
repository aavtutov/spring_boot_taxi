package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

@Mapper
public interface ClientMapper {

    ClientResponseDTO toResponseDto(ClientEntity client);

    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ClientEntity fromResponseDto(ClientResponseDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ClientEntity fromCreateDto(ClientCreateDTO dto);
}
