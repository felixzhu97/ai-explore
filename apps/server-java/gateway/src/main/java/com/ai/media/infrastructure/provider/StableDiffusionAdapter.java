package com.ai.media.infrastructure.provider;

import com.ai.media.domain.*;
import com.ai.media.domain.exception.MediaException;
import com.ai.media.infrastructure.config.MediaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Stable Diffusion provider implementation using Automatic1111 API.
 * Implements the ImageProvider port from domain layer.
 */
@Component
public class StableDiffusionAdapter implements ImageProvider {

    private static final Logger log = LoggerFactory.getLogger(StableDiffusionAdapter.class);

    private final WebClient webClient;
    private final MediaProperties properties;
    private final ObjectMapper objectMapper;
    private final Random random;

    public StableDiffusionAdapter(WebClient stableDiffusionWebClient,
                                  MediaProperties properties,
                                  ObjectMapper objectMapper) {
        this.webClient = stableDiffusionWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.random = new Random();
    }

    @Override
    public Mono<GeneratedImage> generate(GenerationParams params) {
        long startTime = System.currentTimeMillis();
        String model = params.model() != null ? params.model() 
                : properties.stableDiffusion().defaultModel();

        return Mono.fromCallable(() -> buildSdRequest(params, model))
                .flatMap(request -> Mono.fromCallable(() -> callTextToImgApi(request)))
                .map(response -> buildImage(response, params, startTime))
                .timeout(Duration.ofMinutes(5))
                .doOnError(e -> log.error("Image generation failed: {}", e.getMessage(), e))
                .onErrorResume(e -> {
                    log.warn("SD API unavailable, returning placeholder: {}", e.getMessage());
                    return Mono.just(createPlaceholderImage(params, startTime));
                });
    }

    private StableDiffusionRequest buildSdRequest(GenerationParams params, String model) {
        int seed = params.seed() != null ? params.seed().intValue() : random.nextInt(Integer.MAX_VALUE);
        
        return new StableDiffusionRequest(
                params.prompt(),
                params.negativePrompt(),
                params.width(),
                params.height(),
                params.steps(),
                params.cfgScale(),
                seed,
                1
        );
    }

    private StableDiffusionResponse callTextToImgApi(StableDiffusionRequest request) {
        return webClient.post()
                .uri("/sdapi/v1/txt2img")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseSdResponse)
                .block(); // Blocking for simplicity in adapter
    }

    private StableDiffusionResponse parseSdResponse(JsonNode json) {
        List<String> images = new java.util.ArrayList<>();
        
        if (json.has("images")) {
            for (JsonNode img : json.get("images")) {
                images.add(img.asText());
            }
        }
        
        List<Integer> seeds = new java.util.ArrayList<>();
        if (json.has("parameters")) {
            JsonNode params = json.get("parameters");
            if (params.has("seed")) {
                seeds.add(params.get("seed").asInt());
            }
        }
        
        return new StableDiffusionResponse(images, seeds);
    }

    private GeneratedImage buildImage(StableDiffusionResponse response, 
                                       GenerationParams params, long startTime) {
        if (response.images().isEmpty()) {
            throw new MediaException.GenerationException("No images returned from SD API");
        }
        
        long seed = response.seeds().isEmpty() ? random.nextInt(Integer.MAX_VALUE) 
                : response.seeds().get(0);
        
        return GeneratedImage.builder()
                .base64Data(response.images().get(0))
                .seed(seed)
                .width(params.width())
                .height(params.height())
                .build();
    }

    private GeneratedImage createPlaceholderImage(GenerationParams params, long startTime) {
        log.info("Creating placeholder image for SD generation");
        return GeneratedImage.builder()
                .base64Data("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
                .seed(random.nextInt(Integer.MAX_VALUE))
                .width(params.width())
                .height(params.height())
                .build();
    }

    @Override
    public Mono<List<ModelInfo>> listModels() {
        return webClient.get()
                .uri("/sdapi/v1/sd-models")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<JsonNode>>() {})
                .map(nodes -> nodes.stream()
                        .map(node -> ModelInfo.of(
                                node.path("title").asText(),
                                node.path("model_name").asText(),
                                node.path("hash").asText()
                        ))
                        .collect(Collectors.toList()))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("Failed to fetch SD models: {}", e.getMessage());
                    return Mono.just(getDefaultModels());
                });
    }

    private List<ModelInfo> getDefaultModels() {
        return properties.stableDiffusion().availableModels().stream()
                .map(model -> ModelInfo.of(model, model, "stable-diffusion", 
                        model.equals(properties.stableDiffusion().defaultModel())))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<List<LoraInfo>> listLoras() {
        return webClient.get()
                .uri("/sdapi/v1/loras")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<JsonNode>>() {})
                .map(nodes -> nodes.stream()
                        .map(node -> LoraInfo.of(
                                node.path("name").asText(),
                                node.path("name").asText()
                        ))
                        .collect(Collectors.toList()))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.warn("Failed to fetch LoRAs: {}", e.getMessage());
                    return Mono.just(List.of());
                });
    }

    @Override
    public Mono<Boolean> isHealthy() {
        return webClient.get()
                .uri("/sdapi/v1/progress")
                .retrieve()
                .toEntity(JsonNode.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false)
                .defaultIfEmpty(false);
    }

    // Internal DTOs for SD API

    private record StableDiffusionRequest(
            String prompt,
            String negativePrompt,
            int width,
            int height,
            int numInferenceSteps,
            float guidanceScale,
            int seed,
            int batchSize
    ) {
    }

    private record StableDiffusionResponse(
            List<String> images,
            List<Integer> seeds
    ) {
    }
}
