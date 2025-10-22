package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for defining Cross-Origin Resource Sharing (CORS) rules.
 *
 * <p>
 * This implementation allows the Telegram WebApp and any external client to
 * access the Spring Boot REST endpoints.
 * </p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	/**
	 * Configures global CORS settings for all API endpoints.
	 *
	 * @param registry The registry to define CORS configuration.
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				// TODO: [SECURITY/FIXME] Replace '*' with a strict list of allowed origins
				// (production URLs)
				// This current configuration allows ALL origins for testing purposes.
				.allowedOrigins("*").allowedMethods("GET", "POST", "PATCH", "DELETE").allowedHeaders("*");
	}

}
