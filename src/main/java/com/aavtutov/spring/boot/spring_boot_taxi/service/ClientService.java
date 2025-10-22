package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

public interface ClientService {
	
	ClientEntity updateClient(ClientEntity client);

	ClientEntity registerClient(ClientEntity client);

	ClientEntity findClientByTelegramId(Long telegramId);

	Optional<ClientEntity> findClientOptionalByTelegramId(Long telegramId);

	ClientEntity findClientById(Long clientId);

}
