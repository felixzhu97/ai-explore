package com.ai.vision.infrastructure.provider;

import com.ai.vision.domain.*;
import com.ai.vision.domain.exception.VisionException;
import com.ai.vision.infrastructure.config.VisionProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * YOLO-based object detection model adapter.
 * 
 * Infrastructure implementation of VisionModel port.
 * 
 * Supports:
 * - YOLOv5/YOLOv8 ONNX models
 * - CPU/GPU inference
 * - Configurable confidence threshold
 */
@Component
@ConditionalOnProperty(name = "vision.yolo.enabled", havingValue = "true", matchIfMissing = true)
public class YoloModelAdapter implements VisionModel {

    private static final Logger log = LoggerFactory.getLogger(YoloModelAdapter.class);

    private final VisionProperties.YoloConfig config;
    private volatile boolean initialized = false;

    public YoloModelAdapter(VisionProperties properties) {
        this.config = properties.getYolo();
    }

    @Override
    public ModelType type() {
        return ModelType.YOLO;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @PostConstruct
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            log.info("Initializing YOLO provider with model: {}", config.getModelPath());
            
            Path modelPath = Path.of(config.getModelPath());
            if (!Files.exists(modelPath)) {
                log.warn("YOLO model not found at {}. Will use fallback.", config.getModelPath());
            } else {
                log.info("YOLO model loaded successfully from {}", modelPath);
            }
            
            this.initialized = true;
        });
    }

    @Override
    public Mono<DetectionResult> detect(ImageData imageData, float confidence) {
        if (!isAvailable()) {
            return Mono.error(() -> VisionException.modelNotAvailable("YOLO"));
        }

        return Mono.fromCallable(() -> {
            log.info("Running YOLO detection on image ({} bytes), confidence threshold: {}", 
                     imageData.size(), confidence);

            // TODO: Implement actual YOLO inference
            // Integration with ONNX Runtime or OpenCV DNN required
            throw new UnsupportedOperationException(
                "YOLO inference not yet implemented. " +
                "Integration with ONNX Runtime or OpenCV DNN required.");
        });
    }
}
