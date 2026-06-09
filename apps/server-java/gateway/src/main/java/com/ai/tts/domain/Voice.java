package com.ai.tts.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Voice(
    String id,
    String name,
    String language,
    @JsonProperty("language_name") String languageName,
    String gender,
    String provider,
    @JsonProperty("is_default") boolean isDefault
) {
    public static Voice of(String id, String name, String language, String provider) {
        return new Voice(id, name, language, null, null, provider, false);
    }

    public static Voice defaultVoice(String id, String name, String language, String provider) {
        return new Voice(id, name, language, null, null, provider, true);
    }
}
