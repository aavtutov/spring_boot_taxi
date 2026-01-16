package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for processing incoming Telegram updates and managing user registration.
 * Provides the main entry point for the taxi booking WebApp interaction.
 */
@Slf4j
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

	private final ClientService clientService;
	private final TelegramClient telegramClient;
	private final String webAppUrl;

	public UpdateConsumer(ClientService clientService, @Value("${telegram.bot.token}") String botToken,
			@Value("${web.app.url}") String webAppUrl) {
		this.clientService = clientService;
		this.telegramClient = new OkHttpTelegramClient(botToken);
		this.webAppUrl = webAppUrl;
	}

	@Override
	public void consume(Update update) {
		if (!update.hasMessage()) return;

		Long telegramId = update.getMessage().getFrom().getId();
		String firstName = update.getMessage().getFrom().getFirstName();
		String chatId = update.getMessage().getChatId().toString();

		processUserRegistration(telegramId, firstName, chatId);
		sendWebAppButton(chatId,
				"<b>Press the button to book a ride.</b>\n" + "Start using taxi right in your Telegram.");
	}

	private void processUserRegistration(Long telegramId, String firstName, String chatId) {
        clientService.findByTelegramId(telegramId)
            .ifPresentOrElse(
                client -> updateChatIdIfNeeded(client, chatId),
                () -> registerNewClient(telegramId, firstName, chatId)
            );
    }
	
	private void registerNewClient(Long telegramId, String firstName, String chatId) {
        ClientEntity newClient = new ClientEntity();
        newClient.setTelegramId(telegramId);
        newClient.setFullName(firstName);
        newClient.setTelegramChatId(chatId);
        clientService.save(newClient);
        sendMessage(chatId, "👋 Hi, " + firstName + "! You have successfully registered.");
    }
	
	private void updateChatIdIfNeeded(ClientEntity client, String chatId) {
        if (!chatId.equals(client.getTelegramChatId())) {
            client.setTelegramChatId(chatId);
            clientService.save(client);
        }
    }
	
	private void sendWebAppButton(String chatId, String messageText) {
		WebAppInfo webAppInfo = WebAppInfo.builder().url(webAppUrl).build();
		InlineKeyboardButton button = InlineKeyboardButton.builder().text("Open Application").webApp(webAppInfo).build();
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(button)).build();

		executeMessage(SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
	}

	@Async
	void sendMessage(String chatId, String messageText) {
		executeMessage(SendMessage.builder()
				.text(messageText)
				.chatId(chatId)
				.build());
	}
	
	private void executeMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
        	log.error("Failed to execute Telegram API message for chatId: {}", message.getChatId(), e);
        }
    }
}
