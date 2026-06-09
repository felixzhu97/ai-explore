package com.ai.media.infrastructure.agent;

import com.ai.agents.domain.AgentType;
import com.ai.agents.infrastructure.adapter.AgentAdapter;
import com.ai.agents.presentation.dto.AgentRequestDto;
import com.ai.agents.presentation.dto.AgentResponseDto;
import com.ai.media.application.service.MediaApplicationService;
import com.ai.media.domain.GenerationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Media Agent adapter that integrates with the Gateway framework.
 */
@Component
public class MediaAgentAdapter implements AgentAdapter {

    private static final Logger log = LoggerFactory.getLogger(MediaAgentAdapter.class);

    private final MediaApplicationService mediaService;

    public MediaAgentAdapter(MediaApplicationService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public AgentType getType() {
        return AgentType.MEDIA;
    }

    @Override
    public Mono<AgentResponseDto> execute(com.ai.agents.domain.Conversation conversation, AgentRequestDto request) {
        log.info("Processing media request: {}", request.message());

        return mediaService.isHealthy()
                .flatMap(healthy -> {
                    if (!healthy) {
                        return Mono.just(AgentResponseDto.error(
                                "Image generation service is not available"));
                    }

                    String prompt = request.message();

                    GenerationParams params = GenerationParams.builder()
                            .prompt(prompt)
                            .negativePrompt("blurry, ugly, distorted, low quality")
                            .width(512)
                            .height(512)
                            .steps(25)
                            .cfgScale(7.5f)
                            .build();

                    return mediaService.generate(params)
                            .map(result -> {
                                Map<String, Object> metadata = Map.of(
                                        "images", result.images(),
                                        "seed", result.seed(),
                                        "processingTimeMs", result.processingTimeMs()
                                );
                                return new AgentResponseDto(
                                        String.format("Generated %d image(s) in %.0fms",
                                                result.images().size(),
                                                result.processingTimeMs()),
                                        getType(),
                                        false,
                                        null,
                                        null,
                                        null,
                                        metadata
                                );
                            });
                })
                .onErrorResume(e -> {
                    log.error("Media processing failed: {}", e.getMessage(), e);
                    return Mono.just(AgentResponseDto.error(
                            "Image generation failed: " + e.getMessage()));
                });
    }

    @Override
    public boolean isAvailable() {
        return mediaService != null;
    }
}
