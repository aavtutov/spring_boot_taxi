package com.aavtutov.spring.boot.spring_boot_taxi.service;

public interface TelegramBotService {
	
	void sendMessage(String chatId, String message);

}
