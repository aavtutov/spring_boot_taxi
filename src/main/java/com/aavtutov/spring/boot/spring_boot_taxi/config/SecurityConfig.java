package com.aavtutov.spring.boot.spring_boot_taxi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * NOTE FOR REVIEWER: I decided to use environment variables
	 * (SPRING_SECURITY_USER_NAME/PASSWORD) in the docker-compose file instead of
	 * hardcoded InMemoryUserDetailsManager.
	 * This allows update name/passwd in docker-compose.yml without
	 * changing the source code.
	 */
//    @Bean
//    UserDetailsService userDetailsService(PasswordEncoder encoder) {
//        UserDetails admin = User.withUsername("admin")
//                .password(encoder.encode("admin"))
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(admin);
//    }

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/*/admin/**").hasRole("ADMIN")	// Admin-only routes
						.requestMatchers("/api/**").permitAll()					// WebApp routes (validated internally)
						.anyRequest().permitAll()
				)
				.httpBasic(httpBasic -> {})
				.formLogin(form -> {});

		return http.build();
	}
}
