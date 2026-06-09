package com.ai.tts.infrastructure.config;

import com.ai.tts.domain.TtsProvider;
import com.ai.tts.infrastructure.adapter.CosyVoiceProvider;
import com.ai.tts.infrastructure.adapter.EdgeTtsProvider;
import com.ai.tts.infrastructure.adapter.GptSovitsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class TtsProviderConfig {

    public enum TtsProviderType {
        EDGE("edge"),
        COSYVOICE("cosyvoice"),
        GPT_SOVITS("gpt-sovits"),
        AZURE("azure"),
        GOOGLE("google"),
        ELEVENLABS("elevenlabs"),
        COQUI("coqui");

        private final String value;

        TtsProviderType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static TtsProviderType fromString(String value) {
            for (TtsProviderType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return EDGE;
        }
    }

    @Bean
    public Map<TtsProviderType, TtsProvider> ttsProviders() {
        Map<TtsProviderType, TtsProvider> providers = new EnumMap<>(TtsProviderType.class);
        providers.put(TtsProviderType.EDGE, new EdgeTtsProvider());
        providers.put(TtsProviderType.COSYVOICE, new CosyVoiceProvider());
        providers.put(TtsProviderType.GPT_SOVITS, new GptSovitsProvider());
        return providers;
    }

    @Bean
    public TtsProvider defaultTtsProvider(Map<TtsProviderType, TtsProvider> providers, TtsProperties ttsProperties) {
        TtsProviderType type = TtsProviderType.fromString(ttsProperties.getProvider());
        return providers.getOrDefault(type, providers.get(TtsProviderType.EDGE));
    }

    @Bean
    public List<TtsProvider> allTtsProviders(Map<TtsProviderType, TtsProvider> providers) {
        return List.copyOf(providers.values());
    }
}
