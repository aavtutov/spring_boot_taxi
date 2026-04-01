package com.aavtutov.spring.boot.spring_boot_taxi.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

	@Bean(name = "taskExecutor")
	Executor taskExecutor() {
	    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	    executor.setCorePoolSize(2);
	    executor.setMaxPoolSize(10);
	    executor.setQueueCapacity(500);
	    executor.setThreadNamePrefix("TaxiAsync-");
	    executor.initialize();
	    return executor;
	}
	
}
