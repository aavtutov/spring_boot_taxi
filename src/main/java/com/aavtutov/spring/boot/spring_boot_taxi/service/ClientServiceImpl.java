package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;

/**
 * Default implementation of the {@link ClientService} interface.
 *
 * <p>
 * Handles core business logic related to client management, including
 * registration checks and handling non-existent client scenarios.
 * </p>
 */
@Service
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;

	/**
	 * Constructs the service, injecting the required Data Access Object (DAO) for
	 * client data.
	 *
	 * @param clientRepository The repository used to interact with the client
	 *                         persistence layer.
	 */
	public ClientServiceImpl(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Persists changes to an existing client entity. Note that this
	 *             method relies on the client object having a valid, existing ID
	 *             for proper update/merge operation by JPA.
	 *             </p>
	 */
	@Override
	public ClientEntity updateClient(ClientEntity client) {
		return clientRepository.save(client);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Registers a new client, but only after checking if a client with
	 *             the same {@code telegramId} already exists.
	 *             </p>
	 *
	 * @param client The new {@link ClientEntity} to be registered.
	 * @return The newly created and persisted {@link ClientEntity}.
	 * @throws ClientAlreadyExistsException if a client with the given Telegram ID
	 *                                      already exists.
	 */
	@Override
	public ClientEntity registerClient(ClientEntity client) {
		if (clientRepository.findByTelegramId(client.getTelegramId()).isPresent()) {
			throw new ClientAlreadyExistsException(
					"Client with telegramId " + client.getTelegramId() + " already exists");
		}
		return clientRepository.save(client);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves a client by Telegram ID, throwing an exception if the
	 *             client is not found.
	 *             </p>
	 *
	 * @param telegramId The unique Telegram ID of the client.
	 * @return The found {@link ClientEntity}.
	 * @throws ClientNotFoundException if no client is found with the given Telegram
	 *                                 ID.
	 */
	@Override
	public ClientEntity findClientByTelegramId(Long telegramId) {
		return clientRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new ClientNotFoundException("Client with telegram_id=" + telegramId + " not found"));
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves a client by Telegram ID, returning an {@link Optional}
	 *             for scenarios where the absence of a client is expected or
	 *             tolerated (e.g., login check).
	 *             </p>
	 *
	 * @param telegramId The unique Telegram ID of the client.
	 * @return An {@link Optional<ClientEntity>} which may be empty.
	 */
	@Override
	public Optional<ClientEntity> findClientOptionalByTelegramId(Long telegramId) {
		return clientRepository.findByTelegramId(telegramId);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves a client by internal database ID, throwing an exception
	 *             if the client is not found.
	 *             </p>
	 *
	 * @param clientId The internal database ID of the client.
	 * @return The found {@link ClientEntity}.
	 * @throws ClientNotFoundException if no client is found with the given ID.
	 */
	@Override
	public ClientEntity findClientById(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
	}

}
