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

/**
 * Component responsible for validating the authenticity and integrity of the
 * {@code initData} received from the Telegram WebApp.
 *
 * <p>
 * This process follows the Telegram documentation for cryptographic
 * verification using HMAC-SHA256 hash calculation and the bot's token.
 * </p>
 */
@Component
public class TelegramWebAppAuthValidator {

	private final String botToken;
	private final ObjectMapper objectMapper;

	/**
	 * Constructs the validator, injecting the Telegram Bot Token and the Jackson
	 * ObjectMapper.
	 *
	 * @param botToken     The secret token of the Telegram Bot, used to derive the
	 *                     security key.
	 * @param objectMapper Used for deserializing the 'user' JSON data within
	 *                     initData.
	 */
	public TelegramWebAppAuthValidator(@Value("${telegram.bot.token}") String botToken, ObjectMapper objectMapper) {
		this.botToken = botToken.trim();
		this.objectMapper = objectMapper;
	}

	/**
	 * Validates the integrity of the {@code initData} string received from the
	 * Telegram WebApp.
	 *
	 * <p>
	 * The process includes:
	 * </p>
	 * <ol>
	 * <li>Parsing and URL decoding the query string into a sorted map.</li>
	 * <li>Extracting the received 'hash'.</li>
	 * <li>Calculating the expected hash (HMAC-SHA256) over the sorted data string
	 * using the derived secret key.</li>
	 * <li>Comparing the calculated hash with the received hash.</li>
	 * <li>Extracting and deserializing the 'user' object to return the Telegram
	 * ID.</li>
	 * </ol>
	 *
	 * @param initData The raw query string received from the WebApp client.
	 * @return The authenticated Telegram User ID (Long) if validation succeeds.
	 * @throws SecurityException if the data is invalid, compromised, or crucial
	 *                           fields are missing.
	 */
	public TelegramUserDTO validate(String initData) throws SecurityException {

		Map<String, String> dataMap;

		// 1. Parse and URL decode the query string. Using TreeMap ensures
		// lexicographical sorting.
		dataMap = Arrays.stream(initData.split("&")).map(s -> s.split("=", 2)).collect(Collectors.toMap(a -> a[0],
				a -> a.length > 1 ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : "", (a, b) -> a, TreeMap::new));

		String receivedHash = dataMap.remove("hash");
		if (receivedHash == null) {
			throw new SecurityException("Hash not found in initData");
		}

		// 2. Build the data-check-string (key=value joined by '\n', excluding 'hash')
		String dataCheckString = dataMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("\n"));

		try {
			// 3. Calculate the expected hash
			SecretKeySpec secretKey = getSecretKeySpec();
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKey);

			byte[] calculatedHashBytes = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
			String calculatedHash = bytesToHex(calculatedHashBytes);

			// 4. Compare hashes
			if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
				throw new SecurityException("Data hash mismatch. Request is likely compromised.");
			}

			// 5. Extract and parse user data
			String userData = dataMap.get("user");
			if (userData == null) {
				throw new SecurityException("User data not found in initData");
			}

			try {
				TelegramUserDTO userDTO = objectMapper.readValue(userData, TelegramUserDTO.class);
				Long telegramId = userDTO.getId();

				if (telegramId == null) {
					throw new SecurityException("Telegram ID not found in user data");
				}
				return userDTO;

			} catch (Exception e) {
				throw new SecurityException("Failed to parse user data JSON", e);
			}

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			// Should not happen if the algorithm names are correct
			throw new SecurityException("Internal server error during auth validation", e);
		}
	}

	/**
	 * Derives the secret key required for the HMAC-SHA256 verification.
	 *
	 * <p>
	 * The secret key is calculated as: HMAC-SHA256(botToken, "WebAppData").
	 * </p>
	 *
	 * @return The derived SecretKeySpec for HmacSHA256.
	 * @throws NoSuchAlgorithmException If HmacSHA256 is not available.
	 * @throws InvalidKeyException      If the bot token is invalid.
	 */
	private SecretKeySpec getSecretKeySpec() throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec webAppKey = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(webAppKey);
		byte[] secretKeyBytes = mac.doFinal(this.botToken.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(secretKeyBytes, "HmacSHA256");
	}

	/**
	 * Converts an array of bytes to its hexadecimal string representation.
	 *
	 * @param bytes The byte array to convert.
	 * @return The hexadecimal string.
	 */
	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
