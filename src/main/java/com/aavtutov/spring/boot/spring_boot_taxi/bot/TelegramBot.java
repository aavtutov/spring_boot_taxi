package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

/**
 * Entry point for the Telegram Bot integration.
 * This component registers the bot within the Spring context using Long Polling
 * and delegates update handling to the {@link UpdateConsumer} service.
 */
@Component
public class TelegramBot implements SpringLongPollingBot {

	private final String botToken;
	private final UpdateConsumer updateConsumer;

	public TelegramBot(@Value("${telegram.bot.token}") String botToken, UpdateConsumer updateConsumer) {
		this.botToken = botToken;
		this.updateConsumer = updateConsumer;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public LongPollingUpdateConsumer getUpdatesConsumer() {
		return updateConsumer;
	}

}
