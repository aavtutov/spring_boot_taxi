package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

/**
 * Service for managing client data and Telegram-based authentication.
 */
public interface ClientService {
	
	ClientEntity save(ClientEntity client);
	
	/**
     * Retrieves an existing client or creates a new one based on Telegram init-data.
     */
	ClientEntity getOrCreateClient(TelegramUserDTO tgUser);
	
	/**
     * Strict search: returns client or throws EntityNotFoundException.
     */
	ClientEntity findClientByTelegramId(Long telegramId);
	
	/**
     * Flexible search for Telegram Bot flows (e.g. check registration).
     */
	Optional<ClientEntity> findByTelegramId(Long telegramId);
	
	ClientEntity findClientById(Long clientId);
}
