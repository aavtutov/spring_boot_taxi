package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // apply to any URL-address
				// .allowedOrigins("https://main.dlp5w5qqfney2.amplifyapp.com",
				// "http://localhost:8080", "http://51.21.194.46:8080")
				// temporary for tests
				.allowedOrigins("*").allowedMethods("GET", "POST", "PATCH", "DELETE").allowedHeaders("*");
	}

}
