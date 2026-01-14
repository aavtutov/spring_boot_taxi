package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.ClientMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;
	private final ClientMapper clientMapper;


	@Override
	public ClientEntity updateClient(ClientEntity client) {
		return clientRepository.save(client);
	}

	@Override
	public ClientEntity registerClient(ClientEntity client) {
		clientRepository.findByTelegramId(client.getTelegramId()).ifPresent(c -> {
			throw new ClientAlreadyExistsException("Client already exists: " + client.getTelegramId());
		});
		return clientRepository.save(client);
	}

	@Override
	public ClientEntity findClientByTelegramId(Long telegramId) {
		return clientRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new ClientNotFoundException("Client not found: " + telegramId));
	}

	@Override
	public Optional<ClientEntity> findClientOptionalByTelegramId(Long telegramId) {
		return clientRepository.findByTelegramId(telegramId);
	}

	@Override
	public ClientEntity findClientById(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client not found: " + clientId));
	}

	@Override
	public ClientEntity getOrCreateClient(TelegramUserDTO tgUser) {
		return clientRepository.findByTelegramId(tgUser.getId())
				.map(client -> updateExistingClient(client, tgUser))
				.orElseGet(() -> createNewClient(tgUser));
	}
	
	private ClientEntity updateExistingClient(ClientEntity client, TelegramUserDTO tgUser) {
        client.setFullName(tgUser.getFirstName());
        return clientRepository.save(client);
    }

    private ClientEntity createNewClient(TelegramUserDTO tgUser) {
        return clientRepository.save(clientMapper.toEntity(tgUser));
    }
	
	
}
