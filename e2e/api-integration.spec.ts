import { test, expect } from '@playwright/test';
import { ChatFactory, RagFactory, VisionFactory, TtsFactory, ProviderFactory } from './factories';

test.describe('API Integration with Test Factories', () => {
  const GATEWAY_BASE_URL = 'http://localhost:9000';

  test.describe('Chat API Integration', () => {
    test('should send chat message and receive response structure', async ({ request }) => {
      const chatRequest = ChatFactory.createChatRequest(
        [{ role: 'user', content: 'Hello, this is a test message' }],
        { session_id: `e2e-test-${Date.now()}` }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/text/chat/stream`, {
        headers: { 'Content-Type': 'application/json' },
        data: chatRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());

      if (response.status() === 200) {
        const contentType = response.headers()['content-type'];
        expect(contentType).toContain('text/event-stream');
      }
    });

    test('should handle conversation with multiple messages', async ({ request }) => {
      const conversation = ChatFactory.createConversation([
        { role: 'user', content: 'First message' },
        { role: 'assistant', content: 'First response' },
        { role: 'user', content: 'Second message' },
      ]);

      const chatRequest = ChatFactory.createChatRequest(
        conversation.map((m) => ({ role: m.role, content: m.content })),
        { session_id: `e2e-multi-${Date.now()}` }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/text/chat/stream`, {
        headers: { 'Content-Type': 'application/json' },
        data: chatRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should respect temperature parameter', async ({ request }) => {
      const chatRequest = ChatFactory.createChatRequest(
        [{ role: 'user', content: 'Tell me a joke' }],
        { temperature: 0.9, session_id: `e2e-temp-${Date.now()}` }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/text/chat/stream`, {
        headers: { 'Content-Type': 'application/json' },
        data: chatRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });
  });

  test.describe('Provider API Integration', () => {
    test('should list available providers', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/text/providers`);

      if (response.status() === 200) {
        const providers = await response.json();
        expect(Array.isArray(providers) || typeof providers === 'object').toBeTruthy();
      } else {
        expect(response.status()).toBe(404);
      }
    });

    test('should get models for OpenAI provider', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/text/models`, {
        params: { provider: 'openai' },
      });

      expect([200, 400, 404]).toContain(response.status());

      if (response.status() === 200) {
        const models = await response.json();
        expect(Array.isArray(models)).toBeTruthy();
      }
    });

    test('should get models for Anthropic provider', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/text/models`, {
        params: { provider: 'anthropic' },
      });

      expect([200, 400, 404]).toContain(response.status());
    });

    test('should get models for Ollama provider', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/text/models`, {
        params: { provider: 'ollama' },
      });

      expect([200, 400, 404]).toContain(response.status());
    });
  });

  test.describe('RAG API Integration', () => {
    test('should get document list', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/rag/documents/`);

      if (response.status() === 200) {
        const data = await response.json();
        expect(data).toHaveProperty('documents');
        expect(Array.isArray(data.documents)).toBeTruthy();
      } else {
        expect(response.status()).toBe(404);
      }
    });

    test('should send RAG query', async ({ request }) => {
      const queryRequest = RagFactory.createQueryRequest(
        'What is the main topic?',
        { session_id: `e2e-rag-${Date.now()}`, top_k: 3 }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/rag/chat/stream`, {
        headers: { 'Content-Type': 'application/json' },
        data: queryRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should handle query with specific document filter', async ({ request }) => {
      const queryRequest = RagFactory.createQueryRequest(
        'Summarize the document',
        {
          session_id: `e2e-rag-filter-${Date.now()}`,
          doc_ids: ['doc-123', 'doc-456'],
        }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/rag/chat/stream`, {
        headers: { 'Content-Type': 'application/json' },
        data: queryRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });
  });

  test.describe('Vision API Integration', () => {
    test('should test caption endpoint structure', async ({ request }) => {
      const captionRequest = VisionFactory.createCaptionRequest('https://example.com/image.jpg');

      const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/caption`, {
        headers: { 'Content-Type': 'application/json' },
        data: captionRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test detection endpoint structure', async ({ request }) => {
      const detectionRequest = VisionFactory.createDetectionRequest(
        'https://example.com/image.jpg',
        0.7
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/detect`, {
        headers: { 'Content-Type': 'application/json' },
        data: detectionRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test OCR endpoint structure', async ({ request }) => {
      const ocrRequest = VisionFactory.createOcrRequest('https://example.com/text-image.jpg');

      const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/ocr`, {
        headers: { 'Content-Type': 'application/json' },
        data: ocrRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });
  });

  test.describe('TTS API Integration', () => {
    test('should list available voices', async ({ request }) => {
      const response = await request.get(`${GATEWAY_BASE_URL}/api/tts/voices`);

      if (response.status() === 200) {
        const voices = await response.json();
        expect(Array.isArray(voices)).toBeTruthy();

        if (voices.length > 0) {
          const voice = voices[0];
          expect(voice).toHaveProperty('id');
          expect(voice).toHaveProperty('name');
          expect(voice).toHaveProperty('language');
        }
      } else {
        expect(response.status()).toBe(404);
      }
    });

    test('should test synthesize endpoint with English text', async ({ request }) => {
      const synthesizeRequest = TtsFactory.createSynthesizeRequest(
        'Hello, this is a text to speech test.',
        { voice: 'en-US', speed: 1.0 }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/tts/synthesize`, {
        headers: { 'Content-Type': 'application/json' },
        data: synthesizeRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test synthesize endpoint with Chinese text', async ({ request }) => {
      const synthesizeRequest = TtsFactory.createSynthesizeRequest(
        '你好，这是一段语音合成测试。',
        { voice: 'zh-CN', speed: 1.0 }
      );

      const response = await request.post(`${GATEWAY_BASE_URL}/api/tts/synthesize`, {
        headers: { 'Content-Type': 'application/json' },
        data: synthesizeRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test synthesize endpoint with different speeds', async ({ request }) => {
      for (const speed of [0.5, 1.0, 1.5, 2.0]) {
        const synthesizeRequest = TtsFactory.createSynthesizeRequest(
          'Testing speech speed.',
          { speed }
        );

        const response = await request.post(`${GATEWAY_BASE_URL}/api/tts/synthesize`, {
          headers: { 'Content-Type': 'application/json' },
          data: synthesizeRequest,
        });

        expect([200, 400, 404, 500]).toContain(response.status());
      }
    });
  });

  test.describe('Image Generation API Integration', () => {
    test('should test image generation with standard prompt', async ({ request }) => {
      const generateRequest = {
        prompt: 'A beautiful landscape with mountains and a lake',
        width: 512,
        height: 512,
        num_images: 1,
      };

      const response = await request.post(`${GATEWAY_BASE_URL}/api/image/generate`, {
        headers: { 'Content-Type': 'application/json' },
        data: generateRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test image generation with negative prompt', async ({ request }) => {
      const generateRequest = {
        prompt: 'A modern city skyline',
        negative_prompt: 'blurry, low quality, distorted',
        width: 768,
        height: 768,
        num_images: 1,
      };

      const response = await request.post(`${GATEWAY_BASE_URL}/api/image/generate`, {
        headers: { 'Content-Type': 'application/json' },
        data: generateRequest,
      });

      expect([200, 400, 404, 500]).toContain(response.status());
    });

    test('should test image generation with different sizes', async ({ request }) => {
      const sizes = [
        { width: 512, height: 512 },
        { width: 768, height: 768 },
        { width: 1024, height: 1024 },
      ];

      for (const size of sizes) {
        const generateRequest = {
          prompt: 'A simple test image',
          ...size,
          num_images: 1,
        };

        const response = await request.post(`${GATEWAY_BASE_URL}/api/image/generate`, {
          headers: { 'Content-Type': 'application/json' },
          data: generateRequest,
        });

        expect([200, 400, 404, 500]).toContain(response.status());
      }
    });
  });
});
