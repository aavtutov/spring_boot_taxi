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

/**
 * REST Controller for managing client-related operations.
 *
 * <p>Handles HTTP requests for creating and retrieving {@link ClientEntity} records.
 * The base path for all endpoints is {@code /api/clients}.</p>
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

	private final ClientService clientService;
	private final ClientMapper clientMapper;

	/**
     * Constructs the ClientController, injecting the necessary service and DTO mapper.
     *
     * @param clientService The service layer for client business logic.
     * @param clientMapper The utility for converting between DTOs and entities.
     */
	public ClientController(ClientService clientService, ClientMapper clientMapper) {
		this.clientService = clientService;
		this.clientMapper = clientMapper;
	}

	/**
     * Converts a {@link ClientEntity} to its corresponding {@link ClientResponseDTO}.
     *
     * @param clientEntity The entity to convert.
     * @return The response DTO.
     */
	private ClientResponseDTO toResponseDto(ClientEntity clientEntity) {
		return clientMapper.toResponseDto(clientEntity);
	}

	/**
     * Registers a new client in the system.
     *
     * <p>Endpoint: POST /api/clients</p>
     *
     * @param clientDTO The request body containing client details to create.
     * @return The created client's details as a response DTO.
     */
	@PostMapping
	public ClientResponseDTO registerClient(@RequestBody @Valid ClientCreateDTO clientDTO) {
		ClientEntity client = clientMapper.fromCreateDto(clientDTO);
		ClientEntity registeredClient = clientService.registerClient(client);
		return toResponseDto(registeredClient);
	}

	/**
     * Retrieves a client by their unique Telegram ID.
     *
     * <p>Endpoint: GET /api/clients/by-telegram-id/{id}</p>
     *
     * @param telegramId The unique ID assigned by Telegram.
     * @return The client's details as a response DTO.
     */
	@GetMapping("/by-telegram-id/{id}")
	public ClientResponseDTO showClientByTelegramId(@PathVariable("id") Long telegramId) {
		ClientEntity client = clientService.findClientByTelegramId(telegramId);
		return toResponseDto(client);
	}

	/**
     * Retrieves a client by their unique primary key ID.
     *
     * <p>Endpoint: GET /api/clients/by-id/{id}</p>
     *
     * @param clientId The primary key ID of the client in the database.
     * @return The client's details as a response DTO.
     */
	@GetMapping("/by-id/{id}")
	public ClientResponseDTO showClientById(@PathVariable("id") Long clientId) {
		ClientEntity client = clientService.findClientById(clientId);
		return toResponseDto(client);
	}
}
