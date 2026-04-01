package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.aavtutov.spring.boot.spring_boot_taxi.config.FareProperties;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.ClientRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.OrderResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.OrderMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderCancellationSource;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.event.OrderUpdateEvent;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.ClientNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.MapboxServiceException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.NoContentException;
import com.aavtutov.spring.boot.spring_boot_taxi.exception.OrderNotFoundException;
import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;
import com.aavtutov.spring.boot.spring_boot_taxi.service.validator.OrderValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

	private final ClientService clientService;
	private final DriverService driverService;
	private final OrderRepository orderRepository;
	private final DriverRepository driverRepository;
	private final ClientRepository clientRepository;
	private final OrderValidator orderValidator;
	private final FareCalculator fareCalculator;
	private final MapboxRoutingService mapboxRoutingService;
	private final TelegramBotService telegramBotService;
	private final FareProperties fareProperties;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final OrderMapper orderMapper;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	@Override
	public OrderEntity placeOrder(OrderEntity order, Long clientId) {
		
		ClientEntity client = clientRepository.findByIdWithLock(clientId)
	            .orElseThrow(() -> new ClientNotFoundException("Client not found"));

		orderValidator.throwIfClientHasActiveOrder(client.getId());
		
		updateRouteDetails(order);
		order.setClient(client);
		
		return saveAndNotify(order);
	}
	
	@Transactional
	@Override
	public OrderEntity acceptOrder(Long orderId, Long telegramId) {
		
		OrderEntity order = orderRepository.findByIdWithLock(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found"));
		
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);

		orderValidator.throwIfOrderStatusNotAcceptable(order);
		orderValidator.throwIfDriverNotActive(driver);
		orderValidator.throwIfDriverHasActiveOrder(driver.getId());

		order.setDriver(driver);
		order.setStatus(OrderStatus.ACCEPTED);
		order.setAcceptedAt(Instant.now());
		
		messageClient(order, "🚕💨 Your driver is on the way!");
		return saveAndNotify(order);
	}
	
	@Transactional
	@Override
	public OrderEntity startTrip(Long orderId, Long telegramId) {
		
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
		
		orderValidator.throwIfDriverNotAssignedToOrder(order, driver.getId());
		orderValidator.throwIfDriverNotActive(driver);
		orderValidator.throwIfOrderStatusNotStartable(order);

		order.setStatus(OrderStatus.IN_PROGRESS);
		order.setStartedAt(Instant.now());
		
		messageClient(order, "👋 Your driver has arrived!");
		return saveAndNotify(order);
	}	
	
	
	@Transactional
	@Override
	public OrderEntity completeOrder(Long orderId, Long telegramId) {
		
		OrderEntity order = findOrderByIdOrThrow(orderId);
		DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
		
		orderValidator.throwIfDriverNotAssignedToOrder(order, driver.getId());
		orderValidator.throwIfDriverNotActive(driver);
		orderValidator.throwIfOrderStatusNotCompletable(order);

		order.setCompletedAt(Instant.now());
		order.setActualDuration(calculateActualDuration(order));

		BigDecimal calculatedPrice = fareCalculator.calculateFare(order);
		order.setStatus(OrderStatus.COMPLETED);
		order.setPrice(calculatedPrice);
		order.setTotalPrice(calculatedPrice.add(order.getBonusFare()));
		
		messageClient(order, createCompletionMessage(order));
		return saveAndNotify(order);
	}	
	
	@Transactional
    @Override
    public OrderEntity cancelOrderByDriver(Long orderId, Long telegramId) {
        return cancelOrder(orderId, telegramId, OrderCancellationSource.DRIVER);
    }
	
	@Transactional
    @Override
    public OrderEntity cancelOrderByClient(Long orderId, Long telegramId) {
        return cancelOrder(orderId, telegramId, OrderCancellationSource.CLIENT);
    }
	
    private OrderEntity cancelOrder(Long orderId, Long telegramId, OrderCancellationSource source) {
        
    	OrderEntity order = findOrderByIdOrThrow(orderId);
        
        if (source == OrderCancellationSource.DRIVER) {
            DriverEntity driver = driverService.findDriverByTelegramId(telegramId);
            orderValidator.throwIfDriverNotAssignedToOrder(order, driver.getId());
            orderValidator.throwIfDriverNotActive(driver);
            messageClient(order, createDriverCancellationMessage(order));
        } else {
            ClientEntity client = clientService.findClientByTelegramId(telegramId);
            orderValidator.throwIfClientNotAssignedToOrder(order, client.getId());
            messageDriver(order, createClientCancellationMessage(order));
        }

        orderValidator.throwIfOrderStatusNotCancellable(order);
        order.setStatus(OrderStatus.CANCELED);
        order.setCancellationSource(source);
        order.setCancelledAt(Instant.now());
        
        return saveAndNotify(order);
    }
    
	private OrderEntity saveAndNotify(OrderEntity order) {
		OrderEntity savedOrder = orderRepository.save(order);
		eventPublisher.publishEvent(
				new OrderUpdateEvent(
						savedOrder.getId(),
						savedOrder.getStatus(),
						savedOrder.getCancellationSource()));
		return savedOrder;
	}
	
	@Override
	public List<DriverEntity> findSuitableDrivers(Long orderId) {
		findOrderByIdOrThrow(orderId);

		// TODO: Implement advanced filtering (proximity, vehicle type, etc.).
		return driverRepository.findByStatus(DriverStatus.ACTIVE);
	}
	
	// EVENT LISTENERS
	
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleOrderUpdate(OrderUpdateEvent event) {
		
		boolean shouldUpdate = (event.status() == OrderStatus.PENDING)
                || (event.status() == OrderStatus.ACCEPTED)
                || (event.status() == OrderStatus.CANCELED && event.cancellationSource() == OrderCancellationSource.CLIENT);
		
		if (shouldUpdate) {
			updateAllDriversList();
		}
	}
	
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleClientOrderUpdate(OrderUpdateEvent event) {
		try {
			updateSpecificClient(event.orderId());
		} catch (Exception e) {
			log.error("Failed to notify client via WS on orderId={}", event.orderId(), e);
		}
	}
	
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAsyncRouteCalculation(OrderUpdateEvent event) {
	    if (event.status() != OrderStatus.PENDING) return;

	    try {
	        OrderEntity order = orderRepository.findById(event.orderId())
	                .orElseThrow(() -> new OrderNotFoundException("Order not found for async update"));

	        updateRouteDetails(order);
	        orderRepository.save(order);
	        updateSpecificClient(order.getId());
	    } catch (Exception e) {
	        log.error("Failed to update route details asynchronously for order {}", event.orderId(), e);
	    }
	}
	
	// QUERY METHODS

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
	
	// PRIVATE HELPERS
	
	private void updateRouteDetails(OrderEntity order) {
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
			throw new MapboxServiceException("Failed to calculate route");
		}
	}
	
	private void messageClient(OrderEntity order, String message) {
        Optional.ofNullable(order.getClient().getTelegramChatId())
                .ifPresent(chatId -> telegramBotService.sendMessage(chatId, message));
    }
	
	private void messageDriver(OrderEntity order, String message) {
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
	
	private void updateAllDriversList() {
		List<OrderResponseDTO> pendingOrdersDTOs = 
				orderRepository.findAllByStatusWithClient(OrderStatus.PENDING).stream()
				.map(orderMapper::toResponseDto)
				.toList();
		simpMessagingTemplate.convertAndSend("/topic/available-orders", pendingOrdersDTOs);
	}
	
	private void updateSpecificClient(Long orderId) {
		OrderEntity order = orderRepository
				.findByIdWithClientAndDriver(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found"));
		
		OrderResponseDTO responseDTO = orderMapper.toResponseDto(order);
		String topic = "/topic/order-status/" + orderId;
		simpMessagingTemplate.convertAndSend(topic, responseDTO);
	}
}
