package com.aavtutov.spring.boot.spring_boot_taxi.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.ClientEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;

import lombok.RequiredArgsConstructor;

/**
 * Custom resolver that injects ClientEntity into controller methods.
 * Retrieves the user object by fetching it from the database 
 * using the Telegram user data stored in the request.
 */
@Component
@RequiredArgsConstructor
public class ClientArgumentResolver implements HandlerMethodArgumentResolver {

	private final ClientService clientService;
	
	@Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ClientEntity.class);
    }
	
	@Override
    public Object resolveArgument(MethodParameter parameter, 
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, 
                                  WebDataBinderFactory binderFactory) {

        TelegramUserDTO tgUser = (TelegramUserDTO) webRequest.getAttribute("tgUser", RequestAttributes.SCOPE_REQUEST);

        if (tgUser == null) {
            throw new IllegalStateException("TelegramUserDTO missing in request.");
        }

        return clientService.getOrCreateClient(tgUser);
    }
}
