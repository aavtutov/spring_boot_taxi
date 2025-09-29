package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBotLauncher extends TelegramLongPollingBot {

	@Value("${telegram.bot.username}")
	private String botUserName;

	public TelegramBotLauncher(@Value("${telegram.bot.token}") String botToken) {
		super(botToken);
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			String chatId = update.getMessage().getChatId().toString();
			String messageText = update.getMessage().getText();

			if (messageText.equals("/start")) {
				sendMessage(chatId, "Добро пожаловать в Taxi App! Нажмите кнопку для заказа.");
			}

		}

	}

	@Override
	public String getBotUsername() {
		return botUserName;
	}

	private void sendMessage(String chatId, String messageText) {
		SendMessage message = new SendMessage(chatId, messageText);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

}
