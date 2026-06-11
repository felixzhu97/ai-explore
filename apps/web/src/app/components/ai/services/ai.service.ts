import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// ==================== Types ====================

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface ChatRequest {
  messages: ChatMessage[];
  session_id?: string;
  system_prompt?: string;
  temperature?: number;
  max_tokens?: number;
  provider?: string;
  model?: string;
}

export interface ChatResponse {
  text: string;
  provider: string;
  model: string;
  session_id: string;
  usage?: Record<string, number>;
  finish_reason?: string;
}

export interface ModelInfo {
  name: string;
  provider: string;
  description?: string;
  max_tokens?: number;
}

export interface ProviderInfo {
  name: string;
  display_name: string;
  models: string[];
  status: 'available' | 'configured' | 'unavailable';
  supported_languages?: string[];
  features?: string[];
}

export interface ImageGenerationRequest {
  prompt: string;
  negative_prompt?: string;
  width?: number;
  height?: number;
  num_inference_steps?: number;
  guidance_scale?: number;
  seed?: number;
  num_images?: number;
  style_preset?: string;
}

export interface ImageGenerationResponse {
  images: string[];
  seed: number;
  model: string;
  prompt: string;
  inference_steps: number;
  guidance_scale: number;
  width: number;
  height: number;
  processing_time_ms: number;
  metadata?: Record<string, unknown>;
}

export interface Voice {
  id: string;
  name: string;
  language: string;
  language_name?: string;
  gender?: string;
  provider: string;
  is_default: boolean;
}

export interface SynthesizeRequest {
  text: string;
  voice?: string;
  language?: string;
  speed?: number;
  pitch?: number;
  output_format?: 'mp3' | 'wav' | 'ogg' | 'flac';
}

export interface HealthResponse {
  status: string;
  provider: string;
  model?: string;
  version: string;
}

export interface VisionResult {
  caption?: string;
  detections?: Detection[];
  full_text?: string;
  processing_time_ms?: number;
}

export interface Detection {
  class_name: string;
  confidence: number;
  bbox: [number, number, number, number];
}

export interface SourceDocument {
  text: string;
  score: number;
  metadata: Record<string, unknown>;
}

// ==================== Service URLs ====================

const TEXT_SERVICE_URL = '/api/text';
const VISION_SERVICE_URL = '/api/vision';
const RAG_SERVICE_URL = '/api/rag';
const SPEECH_SERVICE_URL = '/api/tts';
const MEDIA_GEN_SERVICE_URL = '/api/image';

// ==================== AI Service ====================

@Injectable({ providedIn: 'root' })
export class AiService {
  private http = inject(HttpClient);

  // ==================== Chat ====================

  chat(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${TEXT_SERVICE_URL}/chat`, request);
  }

  chatStream(
    request: ChatRequest,
    onChunk: (text: string) => void,
    onDone: () => void,
    onError: (error: Error) => void
  ): void {
    const abortController = new AbortController();

    fetch(`${TEXT_SERVICE_URL}/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
      signal: abortController.signal,
    })
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const reader = response.body!.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        function read() {
          reader.read().then(({ done, value }) => {
            if (done) {
              onDone();
              return;
            }

            buffer += decoder.decode(value, { stream: true });
            const parts = buffer.split('\n\n');
            for (let i = 0; i < parts.length - 1; i++) {
              const part = parts[i];
              const dataLines = part.split('\n').filter(l => l.startsWith('data:'));
              for (const dl of dataLines) {
                const data = dl.slice(5).trim();
                if (data && data !== '[DONE]') {
                  try {
                    const parsed = JSON.parse(data);
                    if (parsed.token) onChunk(parsed.token);
                  } catch {
                    onChunk(data + ' ');
                  }
                }
              }
            }

            buffer = parts[parts.length - 1];
            read();
          }).catch(onError);
        }

        read();
      })
      .catch(onError);
  }

  getTextServiceHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${TEXT_SERVICE_URL}/health`);
  }

  getModels(provider?: string): Observable<ModelInfo[]> {
    let params = new HttpParams();
    if (provider) {
      params = params.set('provider', provider);
    }
    return this.http.get<ModelInfo[]>(`${TEXT_SERVICE_URL}/models`, { params });
  }

  getProviders(): Observable<ProviderInfo[]> {
    return this.http.get<ProviderInfo[]>(`${TEXT_SERVICE_URL}/providers`);
  }

  // ==================== Image Generation ====================

  generateImage(request: ImageGenerationRequest): Observable<ImageGenerationResponse> {
    return this.http.post<ImageGenerationResponse>(`${MEDIA_GEN_SERVICE_URL}/generate`, request);
  }

  getVisionServiceHealth(): Observable<{ status: string; device: string; cuda_available: boolean }> {
    return this.http.get<{ status: string; device: string; cuda_available: boolean }>(
      `${VISION_SERVICE_URL}/health`
    );
  }

  // ==================== Vision Tasks ====================

  captionImage(file: File): Observable<VisionResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VisionResult>(`${VISION_SERVICE_URL}/caption`, formData);
  }

  detectObjects(file: File): Observable<VisionResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VisionResult>(`${VISION_SERVICE_URL}/detect`, formData);
  }

  ocrImage(file: File): Observable<VisionResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VisionResult>(`${VISION_SERVICE_URL}/ocr`, formData);
  }

  // ==================== TTS ====================

  synthesizeSpeech(request: SynthesizeRequest): Observable<Blob> {
    return this.http.post(`${SPEECH_SERVICE_URL}/synthesize`, request, {
      responseType: 'blob',
    });
  }

  getVoices(language?: string): Observable<Voice[]> {
    let params = new HttpParams();
    if (language) {
      params = params.set('language', language);
    }
    return this.http.get<Voice[]>(`${SPEECH_SERVICE_URL}/voices`, { params });
  }

  getSpeechServiceHealth(): Observable<{
    status: string;
    provider: string;
    provider_status: string;
    version: string;
  }> {
    return this.http.get<{
      status: string;
      provider: string;
      provider_status: string;
      version: string;
    }>(`${SPEECH_SERVICE_URL}/health`);
  }

  // ==================== RAG ====================

  getDocuments(): Observable<{ documents: { doc_id: string; filename: string }[] }> {
    return this.http.get<{ documents: { doc_id: string; filename: string }[] }>(
      `${RAG_SERVICE_URL}/documents/`
    );
  }

  uploadDocument(file: File): Observable<{ id: string }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', file.name);
    return this.http.post<{ id: string }>(`${RAG_SERVICE_URL}/documents/upload`, formData);
  }

  deleteDocument(docId: string): Observable<void> {
    return this.http.delete<void>(`${RAG_SERVICE_URL}/documents/${docId}`);
  }

  ragChat(
    request: {
      query: string;
      session_id: string;
      top_k?: number;
      temperature?: number;
      doc_ids?: string[];
    },
    onChunk: (text: string) => void,
    onSources: (sources: SourceDocument[]) => void,
    onDone: () => void,
    onError: (error: Error) => void
  ): void {
    fetch(`${RAG_SERVICE_URL}/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
      .then((response) => {
        if (!response.ok) throw new Error('Failed to get response');
        const reader = response.body!.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        function processText(text: string) {
          // Try to parse as JSON, otherwise treat as plain text
          try {
            const parsed = JSON.parse(text);
            if (parsed.token) onChunk(parsed.token);
            return;
          } catch {}
          // Plain text
          const cleanText = text.replace(/<br>/g, '\n').trim();
          if (cleanText) onChunk(cleanText + ' ');
        }

        function read() {
          reader.read().then(({ done, value }) => {
            if (done) {
              onDone();
              return;
            }

            buffer += decoder.decode(value, { stream: true });

            // Split by double newlines (SSE message separator) or process line by line
            const parts = buffer.split('\n\n');
            for (let i = 0; i < parts.length - 1; i++) {
              const part = parts[i];
              // Check if this is a sources event
              if (part.includes('event:sources')) {
                const dataMatch = part.match(/data:(\[.*\])/);
                if (dataMatch) {
                  try {
                    onSources(JSON.parse(dataMatch[1]));
                  } catch {}
                }
              } else {
                // Extract data values
                const dataLines = part.split('\n').filter(l => l.startsWith('data:'));
                for (const dl of dataLines) {
                  const data = dl.slice(5).trim();
                  if (data && data !== '[DONE]') {
                    processText(data);
                  }
                }
              }
            }

            buffer = parts[parts.length - 1];
            read();
          }).catch(onError);
        }

        read();
      })
      .catch(onError);
  }

  // ==================== Utility ====================

  downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  downloadBase64Image(base64: string, filename: string): void {
    const byteCharacters = atob(base64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'image/png' });
    this.downloadBlob(blob, filename);
  }
}
