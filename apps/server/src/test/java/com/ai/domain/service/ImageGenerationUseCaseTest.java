package com.ai.domain.service;

import com.ai.modules.ai.application.usecase.ImageGenerationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageGenerationUseCase Tests")
class ImageGenerationUseCaseTest {

    @Mock
    private org.springframework.ai.image.ImageModel imageModel;

    private ImageGenerationUseCase service;

    @BeforeEach
    void setUp() {
        service = new ImageGenerationUseCase(imageModel);
    }

    @Nested
    @DisplayName("getAvailableModels")
    class GetAvailableModelsTests {

        @Test
        @DisplayName("should return available models")
        void shouldReturnAvailableModels() {
            String[] models = service.getAvailableModels();

            assertThat(models).containsExactly("dall-e-3", "dall-e-2");
        }
    }

    @Nested
    @DisplayName("getAvailableSizes")
    class GetAvailableSizesTests {

        @Test
        @DisplayName("should return available sizes")
        void shouldReturnAvailableSizes() {
            String[] sizes = service.getAvailableSizes();

            assertThat(sizes).containsExactly("1024x1024", "1024x1792", "1792x1024");
        }
    }

    @Nested
    @DisplayName("getAvailableQualities")
    class GetAvailableQualitiesTests {

        @Test
        @DisplayName("should return available qualities")
        void shouldReturnAvailableQualities() {
            String[] qualities = service.getAvailableQualities();

            assertThat(qualities).containsExactly("standard", "hd");
        }
    }
}
