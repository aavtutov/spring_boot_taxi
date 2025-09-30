package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.aavtutov.spring.boot.spring_boot_taxi.bot.TelegramBotLauncher;

@Configuration
public class TelegramBotInitializer {

	private final TelegramBotLauncher telegramBotLauncher;

	public TelegramBotInitializer(TelegramBotLauncher telegramBotLauncher) {
		this.telegramBotLauncher = telegramBotLauncher;
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void init() throws TelegramApiException {
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(telegramBotLauncher);
			System.out.println(
					"Telegram Bot: " + telegramBotLauncher.getBotUsername() + " successfully registered and started!");
		} catch (TelegramApiException e) {
			System.err.println("Error registering Telegram Bot:");
			e.printStackTrace();
		}
	}
}