package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

	Optional<DriverEntity> findByTelegramId(Long telegramId);

	List<DriverEntity> findByIsOnlineTrue();

}
