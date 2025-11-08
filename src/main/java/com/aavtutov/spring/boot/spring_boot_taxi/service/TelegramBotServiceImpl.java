package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Concrete implementation of the {@link TelegramBotService} responsible for
 * sending messages via the official Telegram Bot API.
 *
 * <p>
 * It utilizes Spring's reactive {@link WebClient} for non-blocking HTTP
 * communication and handles configuration via a Telegram bot token.
 * </p>
 */
@Service
public class TelegramBotServiceImpl implements TelegramBotService {

	/**
	 * The unique authentication token for the Telegram Bot, injected from
	 * application properties.
	 */
	@Value("${telegram.bot.token}")
	private String botToken;

	private final WebClient webClient;

	/**
	 * Constructs the service, configuring the {@link WebClient} to use the base URL
	 * of the Telegram API.
	 *
	 * @param webClientBuilder A builder supplied by Spring for creating the
	 *                         WebClient instance.
	 */
	public TelegramBotServiceImpl(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Sends an asynchronous POST request to the Telegram API's
	 *             {@code sendMessage} endpoint. The method returns immediately, and
	 *             the message sending happens non-blockingly in the background.
	 *             </p>
	 *
	 * @param chatId  The recipient's unique Telegram Chat ID.
	 * @param message The text content of the message.
	 */
	public void sendMessage(String chatId, String message) {
		String urlPath = String.format("/bot%s/sendMessage", botToken);

		webClient.post().uri(urlPath)

				// Rationale: The Telegram API expects 'chat_id' and 'text' as request body
				// parameters.
				.bodyValue(Map.of("chat_id", chatId, "text", message)).retrieve().bodyToMono(String.class)

				// Rationale: Handle any errors during the network call or API response.
				// Errors are logged but not re-thrown to prevent blocking the calling thread.
				.doOnError(error -> {
					System.out.println("Error sending Telegram message: " + error.getMessage());
				})

				// Rationale: The .subscribe() call executes the reactive pipeline
				// asynchronously.
				.subscribe();
	}
}
