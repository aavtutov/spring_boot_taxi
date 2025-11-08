package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ActiveOrderAlreadyExistsException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.MapboxServiceException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.NoContentException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;
import com.aavtutov.spring.boot.spring_boot_taxi.service.validator.OrderValidator;

/**
 * Default implementation of the {@link OrderService} interface.
 *
 * <p>
 * This service manages the complete lifecycle of a taxi order, including
 * interaction with external routing and pricing services, and enforcing status
 * transitions.
 * </p>
 */
@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final DriverRepository driverRepository;
	private final ClientRepository clientRepository;
	private final OrderValidator orderValidator;
	private final FareCalculator fareCalculator;
	private final MapboxRoutingService mapboxRoutingService;
	private final TelegramBotService telegramBotService;

	/**
	 * Defines the statuses considered 'active' for business rules (e.g.,
	 * client/driver cannot place/accept a new order).
	 */
	private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED,
			OrderStatus.IN_PROGRESS);

	/**
	 * Constructs the service, injecting all required repositories and external
	 * service dependencies.
	 */
	public OrderServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository,
			ClientRepository clientRepository, OrderValidator orderValidator, FareCalculator fareCalculator,
			MapboxRoutingService mapboxRoutingService, TelegramBotService telegramBotService) {
		this.orderRepository = orderRepository;
		this.driverRepository = driverRepository;
		this.clientRepository = clientRepository;
		this.orderValidator = orderValidator;
		this.fareCalculator = fareCalculator;
		this.mapboxRoutingService = mapboxRoutingService;
		this.telegramBotService = telegramBotService;
	}

	// --- Private Helper Methods ---

	/**
	 * Retrieves an order by ID or throws an {@link OrderNotFoundException}.
	 */
	private OrderEntity findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with id=" + orderId + " not found"));
	}

	/**
	 * Retrieves a driver by ID or throws a {@link DriverNotFoundException}.
	 */
	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	/**
	 * Retrieves a client by ID or throws a {@link ClientNotFoundException}.
	 */
	private ClientEntity findClientByIdOrThrow(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
	}

	// --- Core Business Logic Methods ---

	/**
	 * @inheritDoc
	 *             <p>
	 *             Validates if the client can place a new order, calculates the
	 *             route metrics using Mapbox, and persists the order with estimated
	 *             distance/duration.
	 *             </p>
	 *
	 * @throws ActiveOrderAlreadyExistsException if the client already has an active
	 *                                           order.
	 * @throws MapboxServiceException            if route calculation fails.
	 */
	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		ClientEntity client = findClientByIdOrThrow(clientId);

		// Business Rule: A client can only have one active order (PENDING, ACCEPTED,
		// IN_PROGRESS).
		boolean hasActiveOrders = orderRepository.existsByClientIdAndStatusIn(clientId, ACTIVE_ORDER_STATUSES);
		if (hasActiveOrders) {
			throw new ActiveOrderAlreadyExistsException("Client id=" + clientId + " already has an active order.");
		}

		Route route;
		try {
			// Rationale: Use Mapbox to get the estimated route details.
			route = mapboxRoutingService.getRoute(order.getStartLongitude(), order.getStartLatitude(),
					order.getEndLongitude(), order.getEndLatitude());
		} catch (RuntimeException e) {
			throw new MapboxServiceException("Failed to calculate route for the order.");
		}

		// Convert Mapbox meters/seconds to kilometers/minutes and set scale.
		double distanceKm = route.getDistance() / 1000.0;
		double durationMin = route.getDuration() / 60.0;

		order.setAproximateDistance(BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP));
		order.setAproximateDuration(BigDecimal.valueOf(durationMin).setScale(2, RoundingMode.HALF_UP));
		order.setClient(client);

		return orderRepository.save(order);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Currently returns all drivers with {@link DriverStatus#ACTIVE}.
	 *             This method is a placeholder for future implementation of
	 *             proximity filtering.
	 *             </p>
	 */
	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		// Rationale: Check existence before proceeding.
		findOrderByIdOrThrow(orderId);

		// TODO: Implement advanced filtering (proximity, vehicle type, etc.).
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Assigns a driver to a PENDING order, validates driver
	 *             availability, and sends a notification to the assigned driver.
	 *             </p>
	 *
	 * @throws ActiveOrderAlreadyExistsException if the driver already has an active
	 *                                           order.
	 */
	@Override
	public OrderEntity acceptOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = findDriverByIdOrThrow(driverId);

		orderValidator.throwIfOrderStatusNotAcceptable(order);

		// Business Rule: A driver can only have one active order.
		boolean hasActiveOrder = orderRepository.existsByDriverIdAndStatusIn(driverId, ACTIVE_ORDER_STATUSES);
		if (hasActiveOrder) {
			throw new ActiveOrderAlreadyExistsException("Driver id=" + driverId + " already has an active order.");
		}

		order.setDriver(driver);
		order.setStatus(OrderStatus.ACCEPTED);
		order.setAcceptedAt(Instant.now());

		OrderEntity savedOrder = orderRepository.save(order);

		// Rationale: Notify the client via Telegram about the acceptance.
		if (order.getDriver().getTelegramChatId() != null) {
			String clientChatId = order.getClient().getTelegramChatId();
			String message = "🚕 Your driver is already on the way!";
			telegramBotService.sendMessage(clientChatId, message);
		}

		return savedOrder;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Marks the order status transition from ACCEPTED to IN_PROGRESS
	 *             and logs the start time.
	 *             </p>
	 */
	@Override
	public OrderEntity startTrip(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotStartable(order); // Must be ACCEPTED

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		return orderRepository.save(order);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Marks the order status transition to COMPLETED, calculates the
	 *             actual trip duration, calculates the final fare, and saves the
	 *             final price details.
	 *             </p>
	 */
	@Override
	public OrderEntity completeOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCompletable(order); // Must be IN_PROGRESS

		order.setCompletedAt(Instant.now());

		// Rationale: Calculate actual trip duration. Assumes startedAt is not null due
		// to validator.
		long seconds = Duration.between(order.getStartedAt(), order.getCompletedAt()).getSeconds();
		BigDecimal minutes = BigDecimal.valueOf(seconds / 60.0).setScale(2, RoundingMode.HALF_UP);
		order.setActualDuration(minutes);

		// Rationale: Use the injected FareCalculator to determine the final price based
		// on actuals.
		BigDecimal calculatedPrice = fareCalculator.calculateFare(order);

		order.setStatus(OrderStatus.COMPLETED);
		order.setPrice(calculatedPrice); // Base price without bonuses
		order.setTotalPrice(calculatedPrice.add(order.getBonusFare())); // Final price including bonus

		OrderEntity savedOrder = orderRepository.save(order);

		// Rationale: Notify the client about the completion and final price.
		if (order.getDriver().getTelegramChatId() != null) {
			String clientChatId = order.getClient().getTelegramChatId();
			String message = "🏁 Your ride was completed!" + "\nTotal Price: " + savedOrder.getTotalPrice();
			telegramBotService.sendMessage(clientChatId, message);
		}

		return savedOrder;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Cancels the order, sets the status to CANCELED, and identifies
	 *             the driver as the source.
	 *             </p>
	 */
	@Override
	public OrderEntity cancelOrderByDriver(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.DRIVER);
		order.setCancelledAt(Instant.now());

		OrderEntity savedOrder = orderRepository.save(order);

		// Rationale: Notify the client about the cancellation by the driver.
		if (order.getDriver().getTelegramChatId() != null) {
			String clientChatId = order.getClient().getTelegramChatId();
			String message = " Your ride was cancelled by the driver";
			telegramBotService.sendMessage(clientChatId, message);
		}

		return savedOrder;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Cancels the order, sets the status to CANCELED, and identifies
	 *             the client as the source. Notifies the assigned driver (if any)
	 *             of the cancellation.
	 *             </p>
	 */
	@Override
	public OrderEntity cancelOrderByClient(Long orderId, Long clientId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfClientNotAssignedToOrder(order, clientId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.CLIENT);
		order.setCancelledAt(Instant.now());

		OrderEntity savedOrder = orderRepository.save(order);

		// Rationale: Notify the assigned driver (if the order was accepted) about the
		// client cancellation.
		if (order.getDriver() != null && order.getDriver().getTelegramChatId() != null) {
			String driverChatId = order.getDriver().getTelegramChatId();
			String message = String.format("Customer cancelled the order #%d\nОт: %s\nДо: %s", order.getId(),
					order.getStartAddress(), order.getEndAddress());

			telegramBotService.sendMessage(driverChatId, message);
		}

		return savedOrder;
	}

	// --- Query Methods ---

	/**
	 * @inheritDoc
	 */
	@Override
	public OrderEntity findOrderById(Long orderId) {
		return findOrderByIdOrThrow(orderId);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves all orders currently in the {@link OrderStatus#PENDING}
	 *             status.
	 *             </p>
	 */
	@Override
	public List<OrderEntity> findAvailableOrders() {
		List<OrderEntity> pendingOrders = orderRepository.findAllByStatus(OrderStatus.PENDING);
		return pendingOrders;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Searches for the single order with status ACCEPTED or IN_PROGRESS
	 *             assigned to the driver.
	 *             </p>
	 */
	@Override
	public Optional<OrderEntity> findActiveOrderByDriver(Long driverId) {
		return orderRepository.findFirstByDriverIdAndStatusIn(driverId,
				List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS));
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves the client's order history, sorted by creation date
	 *             descending.
	 *             </p>
	 */
	@Override
	public List<OrderEntity> findOrdersByClientId(Long clientId) {
		return orderRepository.findAllByClientIdOrderByCreatedAtDesc(clientId);
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves the client's most recently created order, regardless of
	 *             status.
	 *             </p>
	 *
	 * @throws NoContentException if the client has placed no orders at all.
	 */
	@Override
	public OrderEntity findMostRecentOrderByClientId(Long clientId) {
		// Rationale: This retrieves the single most recently created order (TOP 1 by
		// creation date DESC).
		return orderRepository.findTopByClientIdOrderByCreatedAtDesc(clientId)
				.orElseThrow(() -> new NoContentException("Client has no any orders"));
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Retrieves the driver's order history, sorted by creation date
	 *             descending.
	 *             </p>
	 */
	@Override
	public List<OrderEntity> findOrdersByDriverId(Long driverId) {
		return orderRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId);
	}

}
