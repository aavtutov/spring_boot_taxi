package com.aavtutov.spring.boot.spring_boot_taxi.service;

/**
 * Service interface defining the contract for sending asynchronous
 * notifications and messages to users (clients or drivers) via the Telegram Bot
 * API.
 *
 * <p>
 * This abstraction decouples the core business logic from the specific
 * implementation details of the Telegram API or any other messaging platform.
 * </p>
 */
public interface TelegramBotService {

	/**
	 * Sends a text message to a specified recipient's chat.
	 *
	 * @param chatId  The unique identifier for the target chat (e.g., client's or
	 *                driver's Telegram Chat ID).
	 * @param message The text content of the message to be sent.
	 * @throws RuntimeException if the message sending fails (e.g., invalid chat ID,
	 *                          network error).
	 */
	void sendMessage(String chatId, String message);

}
