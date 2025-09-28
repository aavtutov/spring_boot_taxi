package com.aavtutov.spring.boot.spring_boot_taxi.service;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;

@Service
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;

	public ClientServiceImpl(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	@Override
	public ClientEntity registerClient(ClientEntity client) {
		if (clientRepository.findByTelegramId(client.getTelegramId()).isPresent()) {
			throw new ClientAlreadyExistsException(
					"Client with telegramId " + client.getTelegramId() + " already exists");
		}
		return clientRepository.save(client);
	}

	@Override
	public ClientEntity findClientByTelegramId(Long telegramId) {
		return clientRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new ClientNotFoundException("Client with telegram_id=" + telegramId + " not found"));
	}

	@Override
	public ClientEntity findClientById(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
	}

}
