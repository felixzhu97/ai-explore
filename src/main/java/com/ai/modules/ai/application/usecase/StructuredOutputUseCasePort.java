package com.ai.modules.ai.application.usecase;

import com.ai.modules.ai.web.dto.TextAnalysisResult;

public interface StructuredOutputUseCasePort {
    TextAnalysisResult analyzeText(String text);
    TextAnalysisResult analyzeTextWithLanguage(String text, String language);
}
