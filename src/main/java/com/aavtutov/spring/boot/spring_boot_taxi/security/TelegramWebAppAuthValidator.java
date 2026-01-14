package com.aavtutov.spring.boot.spring_boot_taxi.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

	public TelegramWebAppAuthValidator(
			@Value("${telegram.bot.token}") String botToken,
			ObjectMapper objectMapper) {
		this.botToken = botToken.trim();
		this.objectMapper = objectMapper;
	}

	/**
     * Verifies Telegram WebApp initData and returns authenticated user details.
     */
	public TelegramUserDTO validate(String initData) throws SecurityException {

		// 1. Parse into sorted map. TreeMap ensures lexicographical sorting.
		Map<String, String> dataMap = Arrays.stream(initData.split("&"))
				.map(s -> s.split("=", 2))
				.collect(Collectors.toMap(
						a -> a[0],
						a -> a.length > 1 ? URLDecoder.decode(a[1], StandardCharsets.UTF_8) : "",
						(a, b) -> a,
						TreeMap::new
				));

		String receivedHash = dataMap.remove("hash");
		if (receivedHash == null) throw new SecurityException("Hash not found in initData");

		// 2. Prepare data check string
		String dataCheckString = dataMap.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("\n"));

		// 3. Verify Signature
		if (!verifyHash(dataCheckString, receivedHash)) {
			throw new SecurityException("Data integrity violation: Hash mismatch");
		}
		
		// 4. Extract User
		return parseUser(dataMap.get("user"));
	}
	
	private boolean verifyHash(String data, String receivedHash) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(getSecretKeySpec());
            byte[] calculatedHashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return hex(calculatedHashBytes).equalsIgnoreCase(receivedHash);
        } catch (Exception e) {
            throw new SecurityException("Auth algorithm failure", e);
        }
    }

    private TelegramUserDTO parseUser(String json) {
        if (json == null) throw new SecurityException("User data missing");
        try {
            return objectMapper.readValue(json, TelegramUserDTO.class);
        } catch (Exception e) {
            throw new SecurityException("Invalid user JSON format", e);
        }
    }

	private SecretKeySpec getSecretKeySpec() throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return new SecretKeySpec(mac.doFinal(this.botToken.getBytes(StandardCharsets.UTF_8)), "HmacSHA256");
	}

	private String hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
