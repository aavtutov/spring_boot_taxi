package com.aavtutov.spring.boot.spring_boot_taxi.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TelegramWebAppAuthValidator {

	private final String botToken;
	private final ObjectMapper objectMapper;

	public TelegramWebAppAuthValidator(@Value("${telegram.bot.token}") String botToken, ObjectMapper objectMapper) {
		this.botToken = botToken.trim();
		this.objectMapper = objectMapper;
	}

	public Long validate(String initData) throws SecurityException {

		Map<String, String> dataMap;
		dataMap = Arrays.stream(initData.split("&")).map(s -> s.split("=", 2)).collect(Collectors.toMap(a -> a[0],
				a -> a.length > 1 ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : "", (a, b) -> a, TreeMap::new));

		String receivedHash = dataMap.remove("hash");
		if (receivedHash == null) {
			throw new SecurityException("Hash not found in initData.");
		}

		String dataCheckString = dataMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("\n"));

		try {
			SecretKeySpec secretKey = getSecretKeySpec();
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKey);

			byte[] calculatedHashBytes = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
			String calculatedHash = bytesToHex(calculatedHashBytes);

			if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
				throw new SecurityException("Data hash mismatch. Request is likely compromised.");
			}

			String userData = dataMap.get("user");
			if (userData == null) {
				throw new SecurityException("User data not found in initData.");
			}

			try {
				TelegramUserDTO userDTO = objectMapper.readValue(userData, TelegramUserDTO.class);
				Long telegramId = userDTO.getId();

				if (telegramId == null) {
					throw new SecurityException("Telegram ID not found in user data.");
				}
				return telegramId;

			} catch (Exception e) {
				throw new SecurityException("Failed to parse user data JSON", e);
			}

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new SecurityException("Internal server error during auth validation.", e);
		}
	}

	private SecretKeySpec getSecretKeySpec() throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec webAppKey = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(webAppKey);
		byte[] secretKeyBytes = mac.doFinal(this.botToken.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(secretKeyBytes, "HmacSHA256");
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
