export class ChatFactory {
  static createMessage(
    role: 'user' | 'assistant' | 'system',
    content: string,
    overrides?: Partial<{
      id: string;
      timestamp: number;
      sources: unknown[];
    }>
  ) {
    return {
      id: overrides?.id ?? `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      role,
      content,
      timestamp: overrides?.timestamp ?? Date.now(),
      ...(role === 'assistant' && { sources: overrides?.sources ?? [] }),
    };
  }

  static createUserMessage(content: string) {
    return this.createMessage('user', content);
  }

  static createAssistantMessage(content: string, sources?: unknown[]) {
    return this.createMessage('assistant', content, { sources });
  }

  static createConversation(messages: Array<{ role: 'user' | 'assistant'; content: string }>) {
    return messages.map((m) => this.createMessage(m.role, m.content));
  }

  static createChatRequest(
    messages: Array<{ role: string; content: string }>,
    overrides?: Partial<{
      session_id: string;
      system_prompt: string;
      temperature: number;
      max_tokens: number;
      provider: string;
      model: string;
    }>
  ) {
    return {
      messages,
      session_id: overrides?.session_id ?? `session_${Date.now()}`,
      system_prompt: overrides?.system_prompt ?? 'You are a helpful assistant.',
      temperature: overrides?.temperature ?? 0.7,
      max_tokens: overrides?.max_tokens ?? 2048,
      provider: overrides?.provider ?? 'openai',
      model: overrides?.model ?? 'gpt-4o-mini',
    };
  }
}

export class RagFactory {
  static createQueryRequest(
    query: string,
    overrides?: Partial<{
      session_id: string;
      top_k: number;
      temperature: number;
      doc_ids: string[];
    }>
  ) {
    return {
      query,
      session_id: overrides?.session_id ?? `rag_session_${Date.now()}`,
      top_k: overrides?.top_k ?? 5,
      temperature: overrides?.temperature ?? 0.7,
      ...(overrides?.doc_ids && { doc_ids: overrides.doc_ids }),
    };
  }

  static createDocumentUpload(fileName: string, overrides?: Partial<{ title: string }>) {
    return {
      fileName,
      title: overrides?.title ?? fileName,
    };
  }

  static createSourceDocument(text: string, score: number, metadata?: Record<string, unknown>) {
    return {
      text,
      score,
      metadata: metadata ?? {},
    };
  }
}

export class VisionFactory {
  static createCaptionRequest(imageUrl: string) {
    return { imageUrl, task: 'caption' };
  }

  static createDetectionRequest(imageUrl: string, confidence = 0.5) {
    return { imageUrl, confidence };
  }

  static createOcrRequest(imageUrl: string) {
    return { imageUrl, task: 'ocr' };
  }

  static createMockDetection(
    className: string,
    confidence: number,
    bbox: [number, number, number, number]
  ) {
    return { class_name: className, confidence, bbox };
  }

  static createCaptionResult(caption: string, processingTimeMs?: number) {
    return {
      caption,
      ...(processingTimeMs && { processing_time_ms: processingTimeMs }),
    };
  }

  static createDetectionResult(detections: unknown[], processingTimeMs?: number) {
    return {
      detections,
      ...(processingTimeMs && { processing_time_ms: processingTimeMs }),
    };
  }

  static createOcrResult(fullText: string, processingTimeMs?: number) {
    return {
      full_text: fullText,
      ...(processingTimeMs && { processing_time_ms: processingTimeMs }),
    };
  }
}

export class TtsFactory {
  static createSynthesizeRequest(
    text: string,
    overrides?: Partial<{
      voice: string;
      speed: number;
      output_format: 'mp3' | 'wav' | 'ogg' | 'flac';
    }>
  ) {
    return {
      text,
      voice: overrides?.voice ?? 'en-US',
      speed: overrides?.speed ?? 1.0,
      output_format: overrides?.output_format ?? 'mp3',
    };
  }

  static createVoice(
    id: string,
    name: string,
    language: string,
    provider = 'default',
    isDefault = false
  ) {
    return { id, name, language, provider, is_default: isDefault };
  }
}

export class ImageGenFactory {
  static createGenerateRequest(
    prompt: string,
    overrides?: Partial<{
      negative_prompt: string;
      width: number;
      height: number;
      num_images: number;
    }>
  ) {
    return {
      prompt,
      negative_prompt: overrides?.negative_prompt ?? '',
      width: overrides?.width ?? 1024,
      height: overrides?.height ?? 1024,
      num_images: overrides?.num_images ?? 1,
    };
  }

  static createGenerateResponse(images: string[], seed: number) {
    return { images, seed };
  }
}

export class ProviderFactory {
  static createProvider(
    name: string,
    displayName: string,
    models: string[],
    status = 'available'
  ) {
    return {
      name,
      display_name: displayName,
      models,
      status,
    };
  }

  static createModel(name: string, provider: string) {
    return { name, provider };
  }

  static getDefaultProviders() {
    return [
      this.createProvider('openai', 'OpenAI', ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo']),
      this.createProvider('anthropic', 'Anthropic Claude', ['claude-sonnet-4-20250514', 'claude-opus-4-20250514']),
      this.createProvider('ollama', 'Ollama (Local)', ['qwen2.5:7b', 'qwen2.5:14b', 'llama3.2:3b']),
    ];
  }
}
