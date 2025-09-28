package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

@Component
public class ClientMapper {

	public ClientResponseDTO toResponseDto(ClientEntity client) {

		ClientResponseDTO dto = new ClientResponseDTO();
		dto.setId(client.getId());
		dto.setTelegramId(client.getTelegramId());
		dto.setFullName(client.getFullName());

		return dto;
	}

	public ClientEntity fromResponseDto(ClientResponseDTO dto) {

		ClientEntity entity = new ClientEntity();
		entity.setId(dto.getId());
		entity.setTelegramId(dto.getTelegramId());
		entity.setFullName(dto.getFullName());

		return entity;
	}

	public ClientEntity fromCreateDto(ClientCreateDTO dto) {

		ClientEntity entity = new ClientEntity();
		entity.setTelegramId(dto.getTelegramId());
		entity.setPhoneNumber(dto.getPhoneNumber());
		entity.setFullName(dto.getFullName());

		return entity;
	}
}
