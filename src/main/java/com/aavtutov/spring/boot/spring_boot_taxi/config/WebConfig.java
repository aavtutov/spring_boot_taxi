package com.aavtutov.spring.boot.spring_boot_taxi.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.aavtutov.spring.boot.spring_boot_taxi.security.ClientArgumentResolver;
import com.aavtutov.spring.boot.spring_boot_taxi.security.DriverArgumentResolver;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramAuthInterceptor;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramUserArgumentResolver;

import lombok.RequiredArgsConstructor;

/**
 * Web configuration class for registering custom interceptors and argument resolvers.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	
	private final TelegramAuthInterceptor authInterceptor;
	private final TelegramUserArgumentResolver userResolver;
	private final ClientArgumentResolver clientResolver;
	private final DriverArgumentResolver driverResolver;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**", "/driver/**", "/client/**")
                .excludePathPatterns("/api/public/**");
    }
	
	@Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userResolver);
        resolvers.add(clientResolver);
        resolvers.add(driverResolver);
    }
}
