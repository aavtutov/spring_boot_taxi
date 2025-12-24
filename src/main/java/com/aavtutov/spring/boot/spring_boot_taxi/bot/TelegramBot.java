package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;


@Component
public class TelegramBot implements SpringLongPollingBot {

	private final String botToken;
	private final UpdateConsumer updateConsumer;

	/**
     * Constructs the TelegramBot bean, injecting the bot token from configuration
     * and the service responsible for update processing.
     *
     * @param botToken The token of the bot, fetched from the Spring environment (e.g., application.properties).
     * @param updateConsumer The consumer service dedicated to handling Telegram updates.
     */
	public TelegramBot(@Value("${telegram.bot.token}") String botToken, UpdateConsumer updateConsumer) {
		this.botToken = botToken;
		this.updateConsumer = updateConsumer;
	}

	/**
     * Returns the token used by the Telegram bot API.
     *
     * @return The bot token string.
     */
	@Override
	public String getBotToken() {
		return botToken;
	}

	/**
     * Returns the consumer responsible for processing all incoming updates.
     *
     * @return The {@link LongPollingUpdateConsumer} instance.
     */
	@Override
	public LongPollingUpdateConsumer getUpdatesConsumer() {
		return updateConsumer;
	}

}
