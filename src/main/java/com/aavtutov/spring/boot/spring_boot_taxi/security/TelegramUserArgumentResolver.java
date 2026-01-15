package com.aavtutov.spring.boot.spring_boot_taxi.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;

/**
 * Custom resolver that injects TelegramUserDTO into controller methods.
 * Retrieves the user object previously stored in the request by the Interceptor.
 */
@Component
public class TelegramUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(TelegramUserDTO.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		Object user = webRequest.getAttribute("tgUser", RequestAttributes.SCOPE_REQUEST);
		
		if (user == null) {
            // If the interceptor was skipped for some reason
            throw new IllegalStateException("TelegramUserDTO not found in request attributes.");
        }
		
		return user;
	}
}
