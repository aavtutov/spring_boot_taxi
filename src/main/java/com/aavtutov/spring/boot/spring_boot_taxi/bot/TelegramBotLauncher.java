package com.aavtutov.spring.boot.spring_boot_taxi.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramBotLauncher extends TelegramLongPollingBot {

	@Override
	public void onUpdateReceived(Update update) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
