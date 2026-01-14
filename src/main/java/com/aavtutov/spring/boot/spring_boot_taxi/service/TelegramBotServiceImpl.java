package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TelegramBotServiceImpl implements TelegramBotService {

	private final WebClient webClient;
	private final String botToken;

	public TelegramBotServiceImpl(
			WebClient.Builder webClientBuilder,
			@Value("${telegram.bot.token}") String botToken) {
		this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
		this.botToken = botToken;
	}

	public void sendMessage(String chatId, String message) {
		String urlPath = String.format("/bot%s/sendMessage", botToken);

		webClient.post()
				.uri(urlPath)
				.bodyValue(Map.of("chat_id", chatId, "text", message))
				.retrieve()
				.bodyToMono(String.class)
				.timeout(Duration.ofSeconds(5))
				.doOnError(error -> {log.error("Telegram API error [chatId: {}]: {}", chatId, error.getMessage());})
				.onErrorResume(e -> Mono.empty())
				.subscribe();
	}
}
