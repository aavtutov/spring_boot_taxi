package com.aavtutov.spring.boot.spring_boot_taxi.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.aavtutov.spring.boot.spring_boot_taxi.dto.telegram.TelegramUserDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverEntity;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DriverArgumentResolver implements HandlerMethodArgumentResolver {
	
	private final DriverService driverService;
	
	@Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(DriverEntity.class);
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

        return driverService.findDriverByTelegramId(tgUser.getId());
    }
}
