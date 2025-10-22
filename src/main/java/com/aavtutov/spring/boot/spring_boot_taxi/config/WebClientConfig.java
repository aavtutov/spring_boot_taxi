package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration class for creating and exposing the {@link WebClient} bean.
 *
 * <p>WebClient is primarily used for making non-blocking HTTP requests to external services, 
 * such as geocoding or route planning APIs.</p>
 */
@Configuration
public class WebClientConfig {

	/**
     * Creates and configures the default {@link WebClient} bean using the provided builder.
     *
     * <p>The builder is auto-configured by Spring Boot and can be customized before calling build().</p>
     *
     * @param builder The auto-configured {@link WebClient.Builder}.
     * @return The configured {@link WebClient} instance, ready for use.
     */
	@Bean
	WebClient webClient(WebClient.Builder builder) {
		
		// No internal comments needed as the logic is trivial (simply building the client).
		return builder.build();
	}

}
