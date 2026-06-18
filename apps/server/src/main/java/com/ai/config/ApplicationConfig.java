package com.ai.config;

import com.ai.domain.repository.ChatSessionRepository;
import com.ai.domain.service.LanguageDetectionService;
import com.ai.adapter.out.persistence.InMemoryChatSessionRepository;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class ApplicationConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        factory.setLocation("/tmp/uploads");
        return factory.createMultipartConfig();
    }

    @Bean
    public ChatSessionRepository chatSessionRepository() {
        return new InMemoryChatSessionRepository();
    }

    @Bean
    public LanguageDetectionService languageDetectionService() {
        return new LanguageDetectionService();
    }
}
