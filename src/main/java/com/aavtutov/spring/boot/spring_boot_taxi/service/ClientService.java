package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Optional;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

/**
 * Service interface defining the business operations related to managing
 * {@link ClientEntity} data.
 *
 * <p>
 * This layer mediates between the Controller/API and the data access layer
 * (Repository), enforcing business rules and handling transactions.
 * </p>
 */
public interface ClientService {

	/**
	 * Updates the details of an existing client in the database.
	 *
	 * <p>
	 * This method should handle merging of changes and ensure data integrity.
	 * </p>
	 *
	 * @param client The {@link ClientEntity} containing the updated fields.
	 * @return The updated and persisted {@link ClientEntity}.
	 * @throws RuntimeException if the client does not exist or if validation fails.
	 */
	ClientEntity updateClient(ClientEntity client);

	/**
	 * Registers a new client in the system.
	 *
	 * <p>
	 * This method is typically called upon initial client authentication or setup.
	 * It should set default values (e.g., creation timestamp) and persist the new
	 * entity.
	 * </p>
	 *
	 * @param client The new {@link ClientEntity} to be registered.
	 * @return The newly created and persisted {@link ClientEntity} with generated
	 *         ID.
	 * @throws RuntimeException if the client with the same ID/phone number already
	 *                          exists.
	 */
	ClientEntity registerClient(ClientEntity client);

	/**
	 * Finds a client by their mandatory Telegram ID.
	 *
	 * <p>
	 * This method is often used when a client must exist for the subsequent
	 * operation (e.g., placing an order).
	 * </p>
	 *
	 * @param telegramId The unique Telegram ID of the client.
	 * @return The found {@link ClientEntity}.
	 * @throws RuntimeException if the client is not found (indicating a required
	 *                          client is missing).
	 */
	ClientEntity findClientByTelegramId(Long telegramId);

	/**
	 * Finds a client by their mandatory Telegram ID, returning the result wrapped
	 * in an {@link Optional}.
	 *
	 * <p>
	 * This method is preferred for checks where a client may or may not exist
	 * (e.g., initial login/registration check).
	 * </p>
	 *
	 * @param telegramId The unique Telegram ID of the client.
	 * @return An {@link Optional<ClientEntity>} which may be empty if no client is
	 *         found.
	 */
	Optional<ClientEntity> findClientOptionalByTelegramId(Long telegramId);

	/**
	 * Finds a client by their internal primary key (ID).
	 *
	 * @param clientId The internal database ID of the client.
	 * @return The found {@link ClientEntity}.
	 * @throws RuntimeException if the client is not found.
	 */
	ClientEntity findClientById(Long clientId);

}
