package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
	@Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
	Optional<OrderEntity> findByIdWithLock(@Param("id") Long id);

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