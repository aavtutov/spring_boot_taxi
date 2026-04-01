package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aavtutov.spring.boot.spring_boot_taxi.dao.OrderRepository;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderChatService {

	private final OrderRepository orderRepository;
	private final TelegramBotService telegramBotService;
	
	private static final List<OrderStatus> ACTIVE_CHAT_STATUSES =
			List.of(OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS);

	public boolean tryForwardMessage(Long senderId, String text) {

		Optional<OrderEntity> orderOpt = orderRepository.findActiveOrderForChat(senderId, ACTIVE_CHAT_STATUSES);

		if (orderOpt.isEmpty()) {
			return false;
		}

		OrderEntity order = orderOpt.get();
		String recipientId;
		String prefix;

		boolean isClient = order.getClient().getTelegramId().equals(senderId);

		if (isClient) {
			if (order.getDriver() == null) {
				return false; // just in case driver is not assigned
			}
			recipientId = order.getDriver().getTelegramChatId();
			prefix = "💬 Message from customer:\n";
		} else {
			recipientId = order.getClient().getTelegramChatId();
			prefix = "💬 Message from driver:\n";
		}

		telegramBotService.sendMessage(recipientId, prefix + text);
		return true;
	}
}
