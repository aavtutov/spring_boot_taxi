package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

/**
 * Spring Data JPA repository for managing {@link ClientEntity} data.
 *
 * <p>
 * Provides standard CRUD operations and custom query methods for client access.
 * </p>
 */
@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

	/**
	 * Finds a single client entity by their unique Telegram ID.
	 *
	 * <p>
	 * This method automatically generates the appropriate query based on the method
	 * name.
	 * </p>
	 *
	 * @param telegramId The unique ID assigned by Telegram to the client.
	 * @return An {@link Optional} containing the found client, or an empty Optional
	 *         if not found.
	 */
	Optional<ClientEntity> findByTelegramId(Long telegramId);

}
