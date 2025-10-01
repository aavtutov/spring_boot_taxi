package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;

@Component
public class TelegramBotLauncher extends TelegramLongPollingBot {

	private final ClientService clientService;

	@Value("${telegram.bot.username}")
	private String botUserName;

	public TelegramBotLauncher(@Value("${telegram.bot.token}") String botToken, ClientService clientService) {
		super(botToken);
		this.clientService = clientService;
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage()) {

			Long telegramId = update.getMessage().getFrom().getId();

			String firstName = update.getMessage().getFrom().getFirstName();
			String lastName = update.getMessage().getFrom().getLastName();
			String fullName = firstName + " " + lastName;

			String chatId = update.getMessage().getChatId().toString();

			if (update.getMessage().hasText() && update.getMessage().getText().equals("/start")) {

				Optional<ClientEntity> optionalClient = clientService.findClientOptionalByTelegramId(telegramId);

				if (optionalClient.isEmpty()) {

					ClientEntity newClient = new ClientEntity();
					newClient.setTelegramId(telegramId);
					newClient.setFullName(fullName);
					clientService.registerClient(newClient);

					sendMessage(chatId, "Hi, " + firstName + "! You have successfully registered.");
				}

			}
			sendWebAppButton(chatId, "Press the button to book a ride.");
		}

	}

	private void sendWebAppButton(String chatId, String messageText) {

		SendMessage message = new SendMessage(chatId, messageText);

		// WebApp data on my server
		WebAppInfo webAppInfo = WebAppInfo.builder().url("https://main.dlp5w5qqfney2.amplifyapp.com/").build();

		// create the btn
		InlineKeyboardButton button = InlineKeyboardButton.builder().text("Book a ride").webApp(webAppInfo).build();

		// create btn row and add WebApp btn
		List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
		keyboardRow.add(button);

		// create keyboard
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(List.of(keyboardRow)).build();

		// link keyboard to message
		message.setReplyMarkup(keyboard);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
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
