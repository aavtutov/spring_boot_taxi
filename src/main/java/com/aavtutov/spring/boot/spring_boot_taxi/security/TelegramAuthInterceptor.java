package com.aavtutov.spring.boot.spring_boot_taxi.security;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor that validates Telegram Web App init data before the request reaches the controller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthInterceptor implements HandlerInterceptor {

	private final TelegramWebAppAuthValidator authValidator;
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
		// Skip interceptor if not a controller method (e.g., static resources)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
		
		String initData = request.getHeader("X-Telegram-Init-Data");

        if (initData == null || initData.isBlank()) {
        	log.warn("Unauthorized access attempt: Missing X-Telegram-Init-Data header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Telegram Init Data");
            return false;
        }

        try {
            TelegramUserDTO user = authValidator.validate(initData);
            request.setAttribute("tgUser", user);
            return true;
        } catch (Exception e) {
        	log.error("Authentication failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Telegram Init Data");
            return false;
        }
    }
}
