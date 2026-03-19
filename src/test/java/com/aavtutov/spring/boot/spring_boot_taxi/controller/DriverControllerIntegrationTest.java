package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.DriverRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverResponseDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramAuthInterceptor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration")
@ActiveProfiles("test")
public class DriverControllerIntegrationTest {
	
	@Autowired
	private DriverRepository driverRepository;
	
	@MockitoBean
	private TelegramAuthInterceptor telegramAuthInterceptor;
	
	@Autowired
    TestRestTemplate testRestTemplate;
	
	@AfterEach
	void tearDown() {
	    driverRepository.deleteAll();
	}
	
	@Test
	@DisplayName("Returns only ACTIVE drivers from DB")
	void testFindAvailableDrivers_shouldReturnOnlyActiveDrivers() throws Exception {
		
		// arrange
		when(telegramAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
		
		driverRepository.save(createDriver(1L, "1L", DriverStatus.ACTIVE));
		driverRepository.save(createDriver(2L, "2L", DriverStatus.INACTIVE));
		
		// act
		ResponseEntity<List<DriverResponseDTO>> response = testRestTemplate.exchange(
				"/api/drivers",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<DriverResponseDTO>>() {
				});
		
		List<DriverResponseDTO> activeDrivers = response.getBody();

		// assert
		assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be 200");
		assertNotNull(activeDrivers);
		assertEquals(1, activeDrivers.size(), "Only one driver should be active");
		assertEquals(1L, activeDrivers.get(0).getTelegramId(), "TelegramId should be 1L");
		assertTrue(activeDrivers.get(0).getId() > 0, "Returned id should be greater than 0");
	}
	
	// Helpers
	private DriverEntity createDriver(Long tgId, String chatId, DriverStatus status) {
	    DriverEntity driver = new DriverEntity();
	    driver.setTelegramId(tgId);
	    driver.setTelegramChatId(chatId);
	    driver.setStatus(status);
	    return driver;
	}

}
