package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderChatService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.TelegramBotService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for processing incoming Telegram updates and managing user registration.
 * Provides the main entry point for the taxi booking WebApp interaction.
 */
@Slf4j
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

	private final ClientService clientService;
	private final OrderChatService orderChatService;
	private final TelegramBotService telegramBotService;
	private final String webAppUrl;

	public UpdateConsumer(
			ClientService clientService,
			OrderChatService orderChatService,
			TelegramBotService telegramBotService,
			@Value("${web.app.url}") String webAppUrl) {
		this.clientService = clientService;
		this.orderChatService = orderChatService;
		this.telegramBotService = telegramBotService;
		this.webAppUrl = webAppUrl.endsWith("/") 
                ? webAppUrl + "loader" 
                : webAppUrl + "/loader";
		log.info("Telegram WebApp Entry Point initialized at: {}", this.webAppUrl);
	}

	@Override
	public void consume(Update update) {
		if (!update.hasMessage() || !update.getMessage().hasText()) return;
		
		Long telegramId = update.getMessage().getFrom().getId();
		String firstName = update.getMessage().getFrom().getFirstName();
		String chatId = update.getMessage().getChatId().toString();
		String messageText = update.getMessage().getText();
		boolean isCommand = messageText.startsWith("/");
		
		if(!isCommand) {
			boolean isProcessedAsChat = orderChatService.tryForwardMessage(telegramId, messageText);
			if(isProcessedAsChat) {
				return;
			} else {
				telegramBotService.sendMessage(chatId, "You don't have an active ride right now, "
						+ "so there's no one to receive your message yet.\n"
						+ "\nBut you can always book one 👇");
			}
		}

		processUserRegistration(telegramId, firstName, chatId);
		sendWebAppButton(chatId,
				"<b>Ready to go?</b>"
				+ "\nOpen app to book your ride in seconds 🚀");
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
        telegramBotService.sendMessage(chatId, "👋 Hi, " + firstName + "! You have successfully registered.");
    }
	
	private void updateChatIdIfNeeded(ClientEntity client, String chatId) {
        if (!chatId.equals(client.getTelegramChatId())) {
            client.setTelegramChatId(chatId);
            clientService.save(client);
        }
    }
	
	private void sendWebAppButton(String chatId, String messageText) {
		
		WebAppInfo webAppInfo = WebAppInfo.builder()
				.url(webAppUrl)
				.build();
		
		InlineKeyboardButton button = InlineKeyboardButton.builder()
				.text("Open Application")
				.webApp(webAppInfo)
				.build();
		
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
				.keyboardRow(new InlineKeyboardRow(button))
				.build();

		telegramBotService.sendMessage(chatId, messageText, keyboard);
	}
}
