package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.ClientResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.ClientMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

import lombok.RequiredArgsConstructor;

/**
 * Controller for managing client-related operations.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

	private final ClientMapper clientMapper;
	
	/**
     * The client is automatically resolved (and created if necessary)
     * by ClientArgumentResolver.
     */
    @GetMapping("/me")
    public ClientResponseDTO getCurrentClient(ClientEntity client) {
        return clientMapper.toResponseDto(client);
    }
}
