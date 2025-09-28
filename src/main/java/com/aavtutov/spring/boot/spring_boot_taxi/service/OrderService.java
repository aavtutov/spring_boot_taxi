package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.util.List;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;

public interface OrderService {

	OrderEntity placeOrder(OrderEntity order, Long clientId);

	List<DriverEntity> findSuitableDrivers(Long orderId);

	OrderEntity acceptOrder(Long orderId, Long driverId);
	
	OrderEntity startTrip(Long orderId, Long driverId);

	OrderEntity completeOrder(Long orderId, Long driverId, BigDecimal finalPrice);

	OrderEntity cancelOrderByDriver(Long orderId, Long driverId);

	OrderEntity cancelOrderByClient(Long orderId, Long clientId);

	OrderEntity findOrderById(Long orderId);

}
