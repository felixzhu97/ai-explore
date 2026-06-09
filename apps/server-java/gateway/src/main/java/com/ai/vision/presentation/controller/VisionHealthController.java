package com.ai.vision.presentation.controller;

import com.ai.vision.application.service.VisionApplicationService;
import com.ai.vision.domain.ModelType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check controller for Vision Service.
 */
@RestController
@RequestMapping("/api/vision")
public class VisionHealthController {

    private final VisionApplicationService visionService;

    public VisionHealthController(VisionApplicationService visionService) {
        this.visionService = visionService;
    }

    /**
     * Liveness probe endpoint.
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        return ResponseEntity.ok(Map.of(
            "status", "alive",
            "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Readiness probe endpoint.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<ModelType, Boolean> status = visionService.getProviderStatus();
        boolean isReady = status.values().stream().anyMatch(Boolean::booleanValue);
        
        if (isReady) {
            return ResponseEntity.ok(Map.of(
                "status", "ready",
                "timestamp", Instant.now().toString()
            ));
        } else {
            return ResponseEntity.status(503).body(Map.of(
                "status", "not ready",
                "timestamp", Instant.now().toString()
            ));
        }
    }
}
