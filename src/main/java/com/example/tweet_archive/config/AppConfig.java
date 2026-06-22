package com.example.tweet_archive.config;

import java.util.concurrent.Executor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableConfigurationProperties({
	GeminiProperties.class,
	AuditProperties.class
})

@Configuration
@EnableAsync
public class AppConfig {

	@Bean(name = "taskExecutor")
	public Executor taskExecutor(){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("gemini-async-");

		executor.initialize();

		return executor;
	}
	
}

