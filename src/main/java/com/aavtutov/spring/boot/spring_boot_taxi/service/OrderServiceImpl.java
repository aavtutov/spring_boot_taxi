package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
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
import com.aavtutov.spring.boot.spring_boot_taxi.exception.DriverNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.MapboxServiceException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.NoContentException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ResourceNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;
import com.aavtutov.spring.boot.spring_boot_taxi.service.validator.OrderValidator;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final DriverRepository driverRepository;
	private final ClientRepository clientRepository;
	private final OrderValidator orderValidator;
	private final FareCalculator fareCalculator;
	private final MapboxRoutingService mapboxRoutingService;
	private final TelegramBotService telegramBotService;
	private final FareProperties fareProperties;
	

	private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED,
			OrderStatus.IN_PROGRESS);

	public OrderServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository,
			ClientRepository clientRepository, OrderValidator orderValidator, FareCalculator fareCalculator,
			MapboxRoutingService mapboxRoutingService, TelegramBotService telegramBotService, FareProperties fareProperties) {
		this.orderRepository = orderRepository;
		this.driverRepository = driverRepository;
		this.clientRepository = clientRepository;
		this.orderValidator = orderValidator;
		this.fareCalculator = fareCalculator;
		this.mapboxRoutingService = mapboxRoutingService;
		this.telegramBotService = telegramBotService;
		this.fareProperties = fareProperties;
	}

	// --- Private Helper Methods ---

	private OrderEntity findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with id=" + orderId + " not found"));
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

//	private ClientEntity findClientByIdOrThrow(Long clientId) {
//		return clientRepository.findById(clientId)
//				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
//	}

	// --- Core Business Logic Methods ---

	@Transactional
	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		
		ClientEntity client = clientRepository.findByIdWithLock(clientId)
	            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

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

	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		findOrderByIdOrThrow(orderId);

		// TODO: Implement advanced filtering (proximity, vehicle type, etc.).
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}

	@Transactional
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
			String message = "🚕💨 Your driver is already on the way!";
			telegramBotService.sendMessage(clientChatId, message);
		}
		return savedOrder;
	}

	@Transactional
	@Override
	public OrderEntity startTrip(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotStartable(order); // Must be ACCEPTED

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		
		OrderEntity savedOrder = orderRepository.save(order);
		// Rationale: Notify the client via Telegram about the arriving.
		
		if (order.getDriver().getTelegramChatId() != null) {
			String clientChatId = order.getClient().getTelegramChatId();
			String message = "👋 Your driver has arrived!";
			telegramBotService.sendMessage(clientChatId, message);
		}
		return savedOrder;
	}

	@Transactional
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
			String message = String.format("🏁 Your ride was completed!"
					+ "\n\n-From: %s"
					+ "\n-To: %s"
					+ "\n\nTotal Price: %.2f %s",
					savedOrder.getStartAddress(),
					savedOrder.getEndAddress(),
					savedOrder.getTotalPrice(),
					fareProperties.getCurrency()
			);
			telegramBotService.sendMessage(clientChatId, message);
		}
		return savedOrder;
	}

	@Transactional
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
			String message = String.format("❌ Driver cancelled the order"
					+ "\n\n-From: %s"
					+ "\n-To: %s",
					order.getStartAddress(),
					order.getEndAddress());
			telegramBotService.sendMessage(clientChatId, message);
		}
		return savedOrder;
	}

	@Transactional
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
			String message = String.format("❌ Customer cancelled the order"
					+ "\n\n-From: %s"
					+ "\n-To: %s",
					order.getStartAddress(),
					order.getEndAddress());
			telegramBotService.sendMessage(driverChatId, message);
		}
		return savedOrder;
	}

	// --- Query Methods ---

	@Transactional
	@Override
	public OrderEntity findOrderById(Long orderId) {
		return findOrderByIdOrThrow(orderId);
	}

	@Transactional
	@Override
	public List<OrderEntity> findAvailableOrders() {
		List<OrderEntity> pendingOrders = orderRepository.findAllByStatus(OrderStatus.PENDING);
		return pendingOrders;
	}

	@Transactional
	@Override
	public Optional<OrderEntity> findActiveOrderByDriver(Long driverId) {
		return orderRepository.findFirstByDriverIdAndStatusIn(driverId,
				List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS));
	}

	@Transactional
	@Override
	public List<OrderEntity> findOrdersByClientId(Long clientId) {
		return orderRepository.findAllByClientIdOrderByCreatedAtDesc(clientId);
	}

	@Transactional
	@Override
	public OrderEntity findMostRecentOrderByClientId(Long clientId) {
		// Rationale: This retrieves the single most recently created order (TOP 1 by
		// creation date DESC).
		return orderRepository.findTopByClientIdOrderByCreatedAtDesc(clientId)
				.orElseThrow(() -> new NoContentException("Client has no any orders"));
	}

	@Transactional
	@Override
	public List<OrderEntity> findOrdersByDriverId(Long driverId) {
		return orderRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId);
	}

}
