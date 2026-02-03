package com.aavtutov.spring.boot.spring_boot_taxi.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.event.DriverStatusChangedEvent;
import com.aavtutov.spring.boot.spring_boot_taxi.service.TelegramBotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverEventListener {

	private final TelegramBotService telegramBotService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDriverStatusChange(DriverStatusChangedEvent event) {
		notifyDriver(event.driver());
	}

	private void notifyDriver(DriverEntity driver) {
		String message = switch (driver.getStatus()) {
			case BANNED -> "Your driver account has been banned!";
			case ACTIVE, INACTIVE -> "🎉 Your driver account is active now!";
			case PENDING_APPROVAL -> "Your driver account status has been changed to: PENDING";
			default -> {
				log.error("Unexpected status: " + driver.getStatus());
				yield null;
			}
		};
		if (message != null) {
			telegramBotService.sendMessage(driver.getTelegramChatId(), message);
		}
	}
}
