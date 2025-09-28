package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.ClientMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

	private final ClientService clientService;
	private final ClientMapper clientMapper;

	public ClientController(ClientService clientService, ClientMapper clientMapper) {
		this.clientService = clientService;
		this.clientMapper = clientMapper;
	}

	private ClientResponseDTO toResponseDto(ClientEntity clientEntity) {
		return clientMapper.toResponseDto(clientEntity);
	}

	@PostMapping
	public ClientResponseDTO registerClient(@RequestBody @Valid ClientCreateDTO clientDTO) {
		ClientEntity client = clientMapper.fromCreateDto(clientDTO);
		ClientEntity registeredClient = clientService.registerClient(client);
		return toResponseDto(registeredClient);
	}

	@GetMapping("/by-telegram-id/{id}")
	public ClientResponseDTO showClientByTelegramId(@PathVariable("id") Long telegramId) {
		ClientEntity client = clientService.findClientByTelegramId(telegramId);
		return toResponseDto(client);
	}

	@GetMapping("/by-id/{id}")
	public ClientResponseDTO showClientById(@PathVariable("id") Long clientId) {
		ClientEntity client = clientService.findClientById(clientId);
		return toResponseDto(client);
	}
}
