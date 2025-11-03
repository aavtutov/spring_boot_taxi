package com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderCreateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

/**
 * Spring Component responsible for mapping data between {@link OrderEntity}
 * (internal persistence object) and various Order DTOs (for API communication).
 *
 * <p>
 * This mapper handles complex nested objects (Client, Driver) and applies
 * necessary conversion logic for creation and response.
 * </p>
 */
@Component
public class OrderMapper {

	private final ClientMapper clientMapper;
	private final DriverMapper driverMapper;

	/**
	 * Constructs the OrderMapper, injecting required mappers for nested entities.
	 *
	 * @param clientMapper Mapper for converting client entities and DTOs.
	 * @param driverMapper Mapper for converting driver entities and DTOs.
	 */
	public OrderMapper(ClientMapper clientMapper, DriverMapper driverMapper) {
		this.clientMapper = clientMapper;
		this.driverMapper = driverMapper;
	}

	/**
	 * Converts an {@link OrderEntity} (internal) to a detailed
	 * {@link OrderResponseDTO} (API output).
	 *
	 * <p>
	 * This involves mapping all lifecycle fields (timestamps, status) and
	 * delegating the mapping of nested Client and Driver entities.
	 * </p>
	 *
	 * @param entity The source order entity.
	 * @return The resulting response DTO.
	 */
	public OrderResponseDTO toResponseDto(OrderEntity entity) {

		OrderResponseDTO dto = new OrderResponseDTO();

		dto.setId(entity.getId());

		// Rationale: Delegate mapping of nested entities. Client is always present.
		dto.setClient(clientMapper.toResponseDto(entity.getClient()));

		// Rationale: Driver may be null if the order is still PENDING, handle null
		// check.
		dto.setDriver(entity.getDriver() != null ? driverMapper.toResponseDto(entity.getDriver()) : null);

		dto.setStatus(entity.getStatus());

		dto.setStartAddress(entity.getStartAddress());
		dto.setEndAddress(entity.getEndAddress());

		dto.setStartLatitude(entity.getStartLatitude());
		dto.setStartLongitude(entity.getStartLongitude());
		dto.setEndLatitude(entity.getEndLatitude());
		dto.setEndLongitude(entity.getEndLongitude());

		dto.setAproximateDistance(entity.getAproximateDistance());
		dto.setAproximateDuration(entity.getAproximateDuration());
		// Assuming ActualDuration field was added to OrderResponseDTO and OrderEntity
		// based on usage here
		dto.setActualDuration(entity.getActualDuration());

		dto.setPrice(entity.getPrice());
		dto.setBonusFare(entity.getBonusFare());

		// Mapping all lifecycle timestamps
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setAcceptedAt(entity.getAcceptedAt());
		dto.setStartedAt(entity.getStartedAt());
		dto.setCompletedAt(entity.getCompletedAt());
		dto.setCancelledAt(entity.getCancelledAt());

		dto.setCancellationSource(entity.getCancellationSource());

		dto.setTotalPrice(entity.getTotalPrice());

		dto.setNotes(entity.getNotes());

		return dto;
	}

	/**
	 * Converts a {@link OrderResponseDTO} back into an {@link OrderEntity}.
	 *
	 * <p>
	 * This method is typically used for internal reconstitution of an entity's
	 * state, often during testing or administrative updates.
	 * </p>
	 *
	 * @param dto The source response DTO.
	 * @return The resulting order entity.
	 */
	public OrderEntity fromResponseDto(OrderResponseDTO dto) {

		OrderEntity entity = new OrderEntity();

		entity.setId(dto.getId());

		// Rationale: Delegate mapping of nested DTOs back to their respective entities.
		// Null checks for driver must be handled within the DriverMapper if necessary.
		entity.setClient(clientMapper.fromResponseDto(dto.getClient()));
		entity.setDriver(driverMapper.fromResponseDto(dto.getDriver()));

		entity.setStatus(dto.getStatus());

		entity.setStartAddress(dto.getStartAddress());
		entity.setEndAddress(dto.getEndAddress());

		entity.setStartLatitude(dto.getStartLatitude());
		entity.setStartLongitude(dto.getStartLongitude());
		entity.setEndLatitude(dto.getEndLatitude());
		entity.setEndLongitude(dto.getEndLongitude());

		entity.setAproximateDistance(dto.getAproximateDistance());
		entity.setAproximateDuration(dto.getAproximateDuration());
		entity.setActualDuration(dto.getActualDuration());

		entity.setPrice(dto.getPrice());
		entity.setBonusFare(dto.getBonusFare());

		entity.setCreatedAt(dto.getCreatedAt());
		entity.setAcceptedAt(dto.getAcceptedAt());
		entity.setStartedAt(dto.getStartedAt());
		entity.setCompletedAt(dto.getCompletedAt());
		entity.setCancelledAt(dto.getCancelledAt());

		entity.setCancellationSource(dto.getCancellationSource());

		entity.setTotalPrice(dto.getTotalPrice());

		entity.setNotes(dto.getNotes());

		return entity;
	}

	/**
	 * Converts an {@link OrderCreateDTO} (API input for creation) to a new
	 * {@link OrderEntity}.
	 *
	 * <p>
	 * This mapper copies initial data (addresses, coordinates, price) and applies
	 * default initialization logic (e.g., setting default bonus fare).
	 * </p>
	 *
	 * @param dto The source creation DTO.
	 * @return The resulting order entity ready for population of Client ID, Status,
	 *         and persistence.
	 */
	public OrderEntity fromCreateDto(OrderCreateDTO dto) {

		OrderEntity entity = new OrderEntity();
		// Rationale: ID, Status, Client, Driver, Timestamps are handled by the service
		// layer after creation.

		entity.setStartAddress(dto.getStartAddress());
		entity.setEndAddress(dto.getEndAddress());

		entity.setStartLatitude(dto.getStartLatitude());
		entity.setStartLongitude(dto.getStartLongitude());
		entity.setEndLatitude(dto.getEndLatitude());
		entity.setEndLongitude(dto.getEndLongitude());

		entity.setPrice(dto.getPrice());

		// Rationale: If bonusFare is null in the DTO, initialize it to BigDecimal.ZERO
		// to ensure non-null integrity and correct calculations in the entity layer.
		entity.setBonusFare(dto.getBonusFare() != null ? dto.getBonusFare() : BigDecimal.ZERO);

		// Rationale: If notes is an empty string, set it to null for cleaner
		// persistence.
		entity.setNotes(dto.getNotes() == "" ? null : dto.getNotes());

		return entity;
	}

}
