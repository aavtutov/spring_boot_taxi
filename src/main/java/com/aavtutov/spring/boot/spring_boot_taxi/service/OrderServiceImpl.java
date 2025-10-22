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

@Service
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final DriverRepository driverRepository;
	private final ClientRepository clientRepository;
	private final OrderValidator orderValidator;
	private final FareCalculator fareCalculator;
	private final MapboxRoutingService mapboxRoutingService;
	private final TelegramBotService telegramBotService;

	private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
			OrderStatus.PENDING,
			OrderStatus.ACCEPTED,
			OrderStatus.IN_PROGRESS);

	public OrderServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository,
			ClientRepository clientRepository, OrderValidator orderValidator, FareCalculator fareCalculator,
			MapboxRoutingService mapboxRoutingService,
			TelegramBotService telegramBotService) {
		this.orderRepository = orderRepository;
		this.driverRepository = driverRepository;
		this.clientRepository = clientRepository;
		this.orderValidator = orderValidator;
		this.fareCalculator = fareCalculator;
		this.mapboxRoutingService = mapboxRoutingService;
		this.telegramBotService = telegramBotService;
	}

	// Helpers
	private OrderEntity findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with id=" + orderId + " not found"));
	}

	private DriverEntity findDriverByIdOrThrow(Long driverId) {
		return driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver with id=" + driverId + " not found"));
	}

	private ClientEntity findClientByIdOrThrow(Long clientId) {
		return clientRepository.findById(clientId)
				.orElseThrow(() -> new ClientNotFoundException("Client with id=" + clientId + " not found"));
	}

	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		ClientEntity client = findClientByIdOrThrow(clientId);

		boolean hasActiveOrders = orderRepository.existsByClientIdAndStatusIn(clientId, ACTIVE_ORDER_STATUSES);
		if (hasActiveOrders) {
			throw new ActiveOrderAlreadyExistsException("Client id=" + clientId + " already has an active order.");
		}

		Route route;
		try {
			route = mapboxRoutingService.getRoute(order.getStartLongitude(), order.getStartLatitude(),
					order.getEndLongitude(), order.getEndLatitude());
		} catch (RuntimeException e) {
			throw new MapboxServiceException("Не удалось рассчитать маршрут для заказа: ");
		}

		double distanceKm = route.getDistance() / 1000.0;
		double durationMin = route.getDuration() / 60.0;

		order.setAproximateDistance(BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP));
		order.setAproximateDuration(BigDecimal.valueOf(durationMin).setScale(2, RoundingMode.HALF_UP));

		order.setClient(client);
		return orderRepository.save(order);
	}

	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		// проверка что заказ существует
		findOrderByIdOrThrow(orderId);
		// пока что заказ показываем всем водителям, позже можно
		// TODO: добавить фильтрацию по критериям заказа
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}

	@Override
	public OrderEntity acceptOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = findDriverByIdOrThrow(driverId);

		orderValidator.throwIfOrderStatusNotAcceptable(order);

		boolean hasActiveOrder = orderRepository.existsByDriverIdAndStatusIn(driverId, ACTIVE_ORDER_STATUSES);
		if (hasActiveOrder) {
			throw new ActiveOrderAlreadyExistsException("Driver id=" + driverId + " already has an active order.");
		}

		order.setDriver(driver);
		order.setStatus(OrderStatus.ACCEPTED);
		order.setAcceptedAt(Instant.now());
		
		OrderEntity savedOrder = orderRepository.save(order);
		
		if (order.getDriver().getTelegramChatId() != null) {
	        String message = "Your driver is already on the way!";
	        telegramBotService.sendMessage(order.getDriver().getTelegramChatId(), message);
	    }
		
		return savedOrder;
	}

	@Override
	public OrderEntity startTrip(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotStartable(order);

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity completeOrder(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCompletable(order);

		order.setCompletedAt(Instant.now());
		long seconds = Duration.between(order.getStartedAt(), order.getCompletedAt()).getSeconds();
		BigDecimal minutes = BigDecimal.valueOf(seconds / 60.0).setScale(2, RoundingMode.HALF_UP);
		order.setActualDuration(minutes);

		BigDecimal calculatedPrice = fareCalculator.calculateFare(order);

		order.setStatus(OrderStatus.COMPLETED);
		order.setPrice(calculatedPrice);
		order.setTotalPrice(calculatedPrice.add(order.getBonusFare()));
		return orderRepository.save(order);
	}

	@Override
	public OrderEntity cancelOrderByDriver(Long orderId, Long driverId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfDriverNotAssignedToOrder(order, driverId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.DRIVER);
		order.setCancelledAt(Instant.now());
		
		OrderEntity savedOrder = orderRepository.save(order);
		
		if (order.getDriver().getTelegramChatId() != null) {
	        String message = "Your ride was cancelled by the driver";
	        telegramBotService.sendMessage(order.getDriver().getTelegramChatId(), message);
	    }
		
		return savedOrder;
	}

	@Override
	public OrderEntity cancelOrderByClient(Long orderId, Long clientId) {
		OrderEntity order = findOrderByIdOrThrow(orderId);

		orderValidator.throwIfClientNotAssignedToOrder(order, clientId);
		orderValidator.throwIfOrderStatusNotCancellable(order);

		order.setStatus(OrderStatus.CANCELED);
		order.setCancellationSource(OrderCancellationSource.CLIENT);
		order.setCancelledAt(Instant.now());
		
		OrderEntity savedOrder = orderRepository.save(order);
		
		if (order.getDriver() != null && order.getDriver().getTelegramChatId() != null) {
	        String message = String.format(
	            "Customer cancelled the order #%d\nОт: %s\nДо: %s",
	            order.getId(),
	            order.getStartAddress(),
	            order.getEndAddress()
	        );

	        telegramBotService.sendMessage(order.getDriver().getTelegramChatId(), message);
	    }
		
		return savedOrder;
	}

	@Override
	public OrderEntity findOrderById(Long orderId) {
		return findOrderByIdOrThrow(orderId);
	}

	@Override
	public List<OrderEntity> findAvailableOrders() {
		List<OrderEntity> pendingOrders = orderRepository.findAllByStatus(OrderStatus.PENDING);
		return pendingOrders;
	}

	@Override
	public Optional<OrderEntity> findActiveOrderByDriver(Long driverId) {
		return orderRepository.findFirstByDriverIdAndStatusIn(driverId,
				List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS));
	}

	@Override
	public List<OrderEntity> findOrdersByClientId(Long clientId) {
		return orderRepository.findAllByClientIdOrderByCreatedAtDesc(clientId);
	}

	@Override
	public OrderEntity findCurrentOrderByClientId(Long clientId) {
		return orderRepository.findTopByClientIdOrderByCreatedAtDesc(clientId)
				.orElseThrow(() -> new NoContentException("Client has no any orders"));
	}

}
