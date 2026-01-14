package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	List<OrderEntity> findAllByStatus(OrderStatus status);

	List<OrderEntity> findByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

	@EntityGraph(attributePaths = {"driver", "client"})
	List<OrderEntity> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

	@EntityGraph(attributePaths = {"driver", "client"})
	List<OrderEntity> findAllByDriverIdOrderByCreatedAtDesc(Long driverId);

	Optional<OrderEntity> findTopByClientIdOrderByCreatedAtDesc(Long clientId);

	Optional<OrderEntity> findFirstByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

	boolean existsByClientIdAndStatusIn(Long clientId, List<OrderStatus> statuses);

	boolean existsByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);

}