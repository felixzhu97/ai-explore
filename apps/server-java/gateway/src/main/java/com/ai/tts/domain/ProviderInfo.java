package com.ai.tts.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProviderInfo(
    String name,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("supported_languages") List<String> supportedLanguages,
    List<String> features,
    @JsonProperty("is_active") boolean isActive
) {
    public static ProviderInfo of(String name, String displayName, List<String> languages, List<String> features) {
        return new ProviderInfo(name, displayName, languages, features, false);
    }

    public static ProviderInfo active(String name, String displayName, List<String> languages, List<String> features) {
        return new ProviderInfo(name, displayName, languages, features, true);
    }
}
