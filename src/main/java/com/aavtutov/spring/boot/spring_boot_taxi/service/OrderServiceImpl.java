package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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

	@Transactional
	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		ClientEntity client = clientRepository.findByIdWithLock(clientId)
	            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

		if (orderRepository.existsByClientIdAndStatusIn(clientId, ACTIVE_ORDER_STATUSES)) {
			throw new ActiveOrderAlreadyExistsException("Client id=" + clientId + " already has an active order.");
		}
		
		updateRouteDetails(order);
		order.setClient(client);

		return orderRepository.save(order);
	}
	
	@Transactional
	@Override
	public OrderEntity acceptOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = findDriverByIdOrThrow(driverId);

		orderValidator.throwIfOrderStatusNotAcceptable(order);

		if (orderRepository.existsByDriverIdAndStatusIn(driverId, ACTIVE_ORDER_STATUSES)) {
			throw new ActiveOrderAlreadyExistsException("Driver id=" + driverId + " already has an active order.");
		}

		order.setDriver(driver);
		order.setStatus(OrderStatus.ACCEPTED);
		order.setAcceptedAt(Instant.now());
		
		notifyClient(order, "🚕💨 Your driver is on the way!");
		return orderRepository.save(order);
	}
	
	@Transactional
	@Override
	public OrderEntity completeOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCompletable(order);

		order.setCompletedAt(Instant.now());
		order.setActualDuration(calculateActualDuration(order));

		BigDecimal calculatedPrice = fareCalculator.calculateFare(order);
		order.setStatus(OrderStatus.COMPLETED);
		order.setPrice(calculatedPrice);
		order.setTotalPrice(calculatedPrice.add(order.getBonusFare()));
		
		notifyClient(order, createCompletionMessage(order));
		return orderRepository.save(order);
	}	
	
	@Transactional
	@Override
	public OrderEntity startTrip(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotStartable(order);

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		
		notifyClient(order, "👋 Your driver has arrived!");
		return orderRepository.save(order);
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

		notifyClient(order, createDriverCancellationMessage(order));
		return orderRepository.save(order);
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

		notifyDriver(order, createClientCancellationMessage(order));
		return orderRepository.save(order);
	}	
	
	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		findOrderByIdOrThrow(orderId);

		// TODO: Implement advanced filtering (proximity, vehicle type, etc.).
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}
	
	// Query Methods

	@Transactional(readOnly = true)
	@Override
	public OrderEntity findOrderById(Long orderId) {
		return findOrderByIdOrThrow(orderId);
	}

	@Transactional(readOnly = true)
	@Override
	public List<OrderEntity> findAvailableOrders() {
		List<OrderEntity> pendingOrders = orderRepository.findAllByStatus(OrderStatus.PENDING);
		return pendingOrders;
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<OrderEntity> findActiveOrderByDriver(Long driverId) {
		return orderRepository.findFirstByDriverIdAndStatusIn(driverId,
				List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS));
	}

	@Transactional(readOnly = true)
	@Override
	public OrderEntity findMostRecentOrderByClientId(Long clientId) {
		return orderRepository.findTopByClientIdOrderByCreatedAtDesc(clientId)
				.orElseThrow(() -> new NoContentException("Client has no any orders"));
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<OrderEntity> findOrdersByClientId(Long clientId) {
		return orderRepository.findAllByClientIdOrderByCreatedAtDesc(clientId);
	}

	@Transactional(readOnly = true)
	@Override
	public List<OrderEntity> findOrdersByDriverId(Long driverId) {
		return orderRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId);
	}
	
	// Private Helpers
	
	private void updateRouteDetails(OrderEntity order) {
		System.out.println("DEBUG: Starting Mapbox request for order...");
		System.out.println("DEBUG: Start: " + order.getStartLongitude() + ", " + order.getStartLatitude());
		try {
			Route route = mapboxRoutingService.getRoute(
					order.getStartLongitude(),
					order.getStartLatitude(),
					order.getEndLongitude(),
					order.getEndLatitude());
			order.setAproximateDistance(
					BigDecimal.valueOf(route.getDistance() / 1000.0)
					.setScale(2, RoundingMode.HALF_UP));
			order.setAproximateDuration(
					BigDecimal.valueOf(route.getDuration() / 60.0)
					.setScale(2, RoundingMode.HALF_UP));
		} catch (Exception e) {
			System.err.println("DEBUG: Mapbox FAILED! Reason: " + e.getMessage());
			throw new MapboxServiceException("Failed to calculate route");
		}
	}
	
	private void notifyClient(OrderEntity order, String message) {
        Optional.ofNullable(order.getClient().getTelegramChatId())
                .ifPresent(chatId -> telegramBotService.sendMessage(chatId, message));
    }
	
	private void notifyDriver(OrderEntity order, String message) {
        Optional.ofNullable(order.getDriver()) // if driver not assigned yet
        		.map(DriverEntity::getTelegramChatId)
                .ifPresent(chatId -> telegramBotService.sendMessage(chatId, message));
    }
	
	private BigDecimal calculateActualDuration(OrderEntity order) {
        long seconds = Duration.between(order.getStartedAt(), order.getCompletedAt()).getSeconds();
        return BigDecimal.valueOf(seconds / 60.0).setScale(2, RoundingMode.HALF_UP);
    }

	private String createCompletionMessage(OrderEntity order) {
		return String.format("🎉 Your ride was completed!"
				+ "\n\n-From: %s"
				+ "\n-To: %s"
				+ "\n\nTotal Price: %.2f %s",
				order.getStartAddress(),
				order.getEndAddress(),
				order.getTotalPrice(),
				fareProperties.getCurrency()
		);
    }
	
	private String createDriverCancellationMessage(OrderEntity order) {
		return String.format("❌ Driver cancelled the order"
				+ "\n\n-From: %s"
				+ "\n-To: %s",
				order.getStartAddress(),
				order.getEndAddress());
    }
	
	private String createClientCancellationMessage(OrderEntity order) {
		return String.format("❌ Customer cancelled the order"
				+ "\n\n-From: %s"
				+ "\n-To: %s",
				order.getStartAddress(),
				order.getEndAddress());
    }	

	private OrderEntity findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with id=" + orderId + " not found"));
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}
}
