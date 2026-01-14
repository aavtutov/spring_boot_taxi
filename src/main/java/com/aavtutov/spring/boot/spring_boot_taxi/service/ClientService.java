package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

/**
 * Service for managing client data and Telegram-based authentication.
 */
public interface ClientService {

	ClientEntity updateClient(ClientEntity client);

	ClientEntity registerClient(ClientEntity client);

	ClientEntity findClientByTelegramId(Long telegramId);

	Optional<ClientEntity> findClientOptionalByTelegramId(Long telegramId);

	ClientEntity findClientById(Long clientId);
	
	/**
     * Retrieves an existing client or creates a new one based on Telegram init-data.
     */
	ClientEntity getOrCreateClient(TelegramUserDTO tgUser);

}
