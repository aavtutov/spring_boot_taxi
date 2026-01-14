package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Cross-Origin Resource Sharing (CORS).
 * Allows the Telegram WebApp to communicate with this API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
	
	private final String webAppUrl;
	
    public CorsConfig(@Value("${web.app.url}") String webAppUrl) {
        this.webAppUrl = webAppUrl;
    }

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(webAppUrl)
				.allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.maxAge(3600);	// Cache pre-flight response for 1 hour
	}
}
