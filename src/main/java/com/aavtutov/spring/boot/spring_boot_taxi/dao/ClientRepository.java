package com.aavtutov.spring.boot.spring_boot_taxi.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

	Optional<ClientEntity> findByTelegramId(Long telegramId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE) 
    @Query("SELECT c FROM ClientEntity c WHERE c.id = :id")
    Optional<ClientEntity> findByIdWithLock(@Param("id") Long id);
}
