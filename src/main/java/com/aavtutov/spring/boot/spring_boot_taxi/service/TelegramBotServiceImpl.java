package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TelegramBotServiceImpl implements TelegramBotService {

	@Value("${telegram.bot.token}")
	private String botToken;

	private final WebClient webClient;

	public TelegramBotServiceImpl(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
	}

	public void sendMessage(String chatId, String message) {
		String urlPath = String.format("/bot%s/sendMessage", botToken);

		webClient.post().uri(urlPath).bodyValue(Map.of("chat_id", chatId, "text", message)).retrieve()
				.bodyToMono(String.class).doOnError(error -> {
					System.out.println("Ошибка при отправке Telegram-сообщения: " + error.getMessage());
				}).subscribe();
	}
}
