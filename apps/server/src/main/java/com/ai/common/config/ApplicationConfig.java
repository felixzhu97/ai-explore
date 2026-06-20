package com.ai.shared.config;

import com.ai.modules.ai.domain.repository.ChatSessionRepository;
import com.ai.domain.service.LanguageDetectionService;
import com.ai.modules.ai.infrastructure.store.InMemoryChatSessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ChatSessionRepository chatSessionRepository() {
        return new InMemoryChatSessionRepository();
    }

    @Bean
    public LanguageDetectionService languageDetectionService() {
        return new LanguageDetectionService();
    }
}
