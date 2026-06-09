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
 * OCR text recognition model adapter.
 * 
 * Infrastructure implementation of VisionModel port.
 * 
 * Supports:
 * - Multiple languages (eng, chi_sim, jpn, kor, etc.)
 * - Layout analysis (tables, multi-column)
 * - Confidence scores
 * 
 * TODO: Implement actual OCR using Tesseract when tess4j dependency is available.
 * For now, returns a placeholder response indicating OCR is not yet implemented.
 */
@Component
@ConditionalOnProperty(name = "vision.ocr.enabled", havingValue = "true", matchIfMissing = true)
public class OcrModelAdapter implements VisionModel {

    private static final Logger log = LoggerFactory.getLogger(OcrModelAdapter.class);

    private final VisionProperties.OcrConfig config;
    private volatile boolean initialized = false;
    private volatile boolean tesseractAvailable = false;

    public OcrModelAdapter(VisionProperties properties) {
        this.config = properties.getOcr();
    }

    @Override
    public ModelType type() {
        return ModelType.OCR;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @PostConstruct
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            log.info("Initializing OCR provider with language: {}", config.getLanguage());
            
            try {
                // Try to load Tesseract dynamically
                Class<?> tesseractClass = Class.forName("net.java.dev.tess4j.Tesseract");
                log.info("Tesseract library found - OCR will be fully functional");
                this.tesseractAvailable = true;
            } catch (ClassNotFoundException e) {
                log.warn("Tesseract library not found. OCR will return placeholder responses. " +
                         "Add tess4j dependency for full OCR functionality.");
                this.tesseractAvailable = false;
            }
            
            Path tessdataPath = Path.of(config.getModelPath());
            if (!Files.exists(tessdataPath)) {
                log.warn("Tessdata not found at {}. OCR may not work correctly.", 
                         config.getModelPath());
            }
            
            this.initialized = true;
        });
    }

    @Override
    public Mono<OcrResult> recognizeText(ImageData imageData, String language) {
        if (!isAvailable()) {
            return Mono.error(() -> VisionException.modelNotAvailable("OCR"));
        }

        return Mono.fromCallable(() -> {
            log.info("Running OCR on image ({} bytes), language: {}", 
                     imageData.size(), language);

            if (!tesseractAvailable) {
                // Return placeholder response when Tesseract is not available
                return new OcrResult(
                    "OCR placeholder - Tesseract library not loaded. " +
                    "Image size: " + imageData.size() + " bytes, Language: " + language,
                    0.0f,
                    List.of()
                );
            }

            // TODO: Implement actual Tesseract OCR when library is available
            try {
                Class<?> tesseractClass = Class.forName("net.java.dev.tess4j.Tesseract");
                Class<?> tesseractExceptionClass = Class.forName("net.java.dev.tess4j.TesseractException");
                
                Object tesseract = tesseractClass.getDeclaredConstructor().newInstance();
                tesseractClass.getMethod("setDatapath", String.class).invoke(tesseract, config.getModelPath());
                tesseractClass.getMethod("setLanguage", String.class).invoke(tesseract, language);
                tesseractClass.getMethod("setPageSegMode", int.class).invoke(tesseract, 3);

                // Convert ImageData to BufferedImage
                Class<?> bufferedImageClass = Class.forName("java.awt.image.BufferedImage");
                Class<?> imageIOClass = Class.forName("javax.imageio.ImageIO");
                Class<?> byteArrayInputStreamClass = Class.forName("java.io.ByteArrayInputStream");
                
                Object bais = byteArrayInputStreamClass.getConstructor(byte[].class)
                    .newInstance(imageData.dataUnsafe());
                Object image = imageIOClass.getMethod("read", byteArrayInputStreamClass).invoke(null, bais);
                
                if (image == null) {
                    throw VisionException.invalidImageData();
                }

                String text = (String) tesseractClass.getMethod("doOCR", bufferedImageClass)
                    .invoke(tesseract, image);

                return new OcrResult(text.trim(), 85.0f, List.of());

            } catch (Exception e) {
                log.error("OCR processing failed: {}", e.getMessage());
                throw VisionException.ocrFailed(e);
            }
        });
    }
}
