package com.aavtutov.spring.boot.spring_boot_taxi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = "spring.autoconfigure.exclude=org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration")
@ActiveProfiles("test")
class SpringBootTaxiApplicationTests {

	@Test
	void contextLoads() {
	}

}
