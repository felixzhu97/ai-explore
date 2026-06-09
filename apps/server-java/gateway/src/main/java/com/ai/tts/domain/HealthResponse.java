package com.ai.tts.domain;

public record HealthResponse(
    String status,
    String provider,
    String providerStatus,
    String version
) {
    public static final String VERSION = "1.0.0";

    public static HealthResponse healthy(String provider) {
        return new HealthResponse("healthy", provider, "healthy", VERSION);
    }

    public static HealthResponse unhealthy(String provider, String reason) {
        return new HealthResponse("unhealthy", provider, reason, VERSION);
    }
}
