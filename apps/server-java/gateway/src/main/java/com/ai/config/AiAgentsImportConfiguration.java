package com.ai.config;

import com.ai.agents.domain.service.AgentRegistry;
import com.ai.agents.domain.service.SupervisorAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to enable AI Agents module components in the Gateway.
 * This configuration imports the new com.ai.agents package structure.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.ai.agents"
})
public class AiAgentsImportConfiguration {

    /**
     * Domain service for agent registration.
     */
    @Bean
    public AgentRegistry agentRegistry() {
        return new AgentRegistry();
    }

    /**
     * Domain service for intent routing.
     */
    @Bean
    public SupervisorAgent supervisorAgent() {
        return new SupervisorAgent();
    }
}
