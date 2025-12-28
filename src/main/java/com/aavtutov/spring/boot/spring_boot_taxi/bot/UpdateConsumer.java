package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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

/**
 * A dedicated consumer component for processing incoming Telegram
 * {@link Update}s in a single-threaded manner.
 *
 * <p>
 * Handles user registration, client updates, and sends the initial message with
 * the WebApp button.
 * </p>
 */
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

	private final ClientService clientService;
	private final TelegramClient telegramClient;
	private final String webAppUrl;

	/**
	 * Constructs the UpdateConsumer, injecting necessary dependencies and
	 * configuration values.
	 *
	 * @param clientService The service layer for managing client data in the
	 *                      database.
	 * @param botToken      The bot's token, used to initialize the TelegramClient.
	 * @param webAppUrl     The external URL for the Telegram WebApp.
	 */
	public UpdateConsumer(ClientService clientService, @Value("${telegram.bot.token}") String botToken,
			@Value("${web.app.url}") String webAppUrl) {
		this.clientService = clientService;

		// Rationale: Using OkHttp client implementation for executing API calls.
		this.telegramClient = new OkHttpTelegramClient(botToken);
		this.webAppUrl = webAppUrl;
	}

	/**
	 * The main entry point for processing an incoming Telegram update.
	 *
	 * <p>
	 * Registers a new client or updates an existing client's chat ID, then sends
	 * the WebApp button.
	 * </p>
	 *
	 * @param update The incoming {@link Update} object from the Telegram API.
	 */
	@Override
	public void consume(Update update) {

		// Only process updates that contain a message (i.e., not inline queries, etc.)
		if (!update.hasMessage())
			return;

		Long telegramId = update.getMessage().getFrom().getId();
		String firstName = update.getMessage().getFrom().getFirstName();
		String chatId = update.getMessage().getChatId().toString();

		Optional<ClientEntity> optionalClient = clientService.findClientOptionalByTelegramId(telegramId);

		if (optionalClient.isEmpty()) {

			// Register new client in the database
			ClientEntity newClient = new ClientEntity();
			newClient.setTelegramId(telegramId);
			newClient.setFullName(firstName);
			newClient.setTelegramChatId(chatId);
			clientService.registerClient(newClient);

			sendMessage(chatId, "👋 Hi, " + firstName + "! You have successfully registered.");
		} else {

			// Existing client: check if chatId needs updating (e.g., user started bot in a
			// new chat/group)
			ClientEntity client = optionalClient.get();

			if (!chatId.equals(client.getTelegramChatId())) {
				client.setTelegramChatId(chatId);
				clientService.updateClient(client);
			}
		}

		// Always send the WebApp button after the client is registered/updated
		sendWebAppButton(chatId,
				"<b>Press the button to book a ride.</b>\n" + "Start using taxi right in your Telegram.");

	}

	/**
	 * Sends a message with an Inline Keyboard containing the WebApp button.
	 *
	 * @param chatId      The ID of the chat to send the message to.
	 * @param messageText The text content of the message (supports HTML parse
	 *                    mode).
	 */
	private void sendWebAppButton(String chatId, String messageText) {

		SendMessage message = new SendMessage(chatId, messageText);
		message.setParseMode("HTML");

		// WebApp link configuration
		WebAppInfo webAppInfo = WebAppInfo.builder().url(webAppUrl).build();

		// Build the Inline Keyboard Button linking to the WebApp
		InlineKeyboardButton button = InlineKeyboardButton.builder().text("Open Hop in").webApp(webAppInfo).build();

		// Create the keyboard structure
		InlineKeyboardRow row = new InlineKeyboardRow(List.of(button));
		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();

		message.setReplyMarkup(keyboard);

		try {
			telegramClient.execute(message);
		} catch (TelegramApiException e) {

			// Log the exception rather than just printing the stack trace in a production
			// application.
			e.printStackTrace();
		}

	}

	/**
	 * Helper method to send a simple text message to a specific chat ID.
	 *
	 * @param chatId      The ID of the chat to send the message to.
	 * @param messageText The text content of the message.
	 */
	private void sendMessage(String chatId, String messageText) {
		SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();

		try {
			telegramClient.execute(message);
		} catch (TelegramApiException e) {

			// Log the exception rather than just printing the stack trace in a production
			// application.
			e.printStackTrace();
		}

	}

}
