package com.aavtutov.spring.boot.spring_boot_taxi.service;

/**
 * Service for sending asynchronous notifications to users via Telegram.
 */
public interface TelegramBotService {

	/**
     * Sends a text message to the specified chat.
     * @param chatId  Target chat identifier.
     * @param message Text content.
     */
	void sendMessage(String chatId, String message);
}
