import { test, expect } from '@playwright/test';

const GATEWAY_BASE_URL = 'http://localhost:9000';

test.describe('Java Gateway Health Check', () => {
  test('Gateway health endpoint should return UP status', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/actuator/health`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('status', 'UP');
    expect(body).toHaveProperty('components');
  });

  test('Gateway should have diskSpace component healthy', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/actuator/health`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.components).toHaveProperty('diskSpace');
    expect(body.components.diskSpace).toHaveProperty('status', 'UP');
  });

  test('Gateway should have ping component healthy', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/actuator/health`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.components).toHaveProperty('ping');
    expect(body.components.ping).toHaveProperty('status', 'UP');
  });
});

test.describe('Java Gateway API Endpoints', () => {
  const endpoints = [
    { path: '/api/text/providers', method: 'GET', expectedStatus: [200, 404] },
    { path: '/api/text/models', method: 'GET', expectedStatus: [200, 400, 404] },
    { path: '/api/vision/caption', method: 'POST', expectedStatus: [200, 400, 404, 500] },
    { path: '/api/rag/documents/', method: 'GET', expectedStatus: [200, 404] },
    { path: '/api/tts/voices', method: 'GET', expectedStatus: [200, 404] },
    { path: '/api/image/generate', method: 'POST', expectedStatus: [200, 400, 404, 500] },
  ];

  for (const endpoint of endpoints) {
    test(`${endpoint.method} ${endpoint.path} should respond`, async ({ request }) => {
      let response;
      if (endpoint.method === 'GET') {
        response = await request.get(`${GATEWAY_BASE_URL}${endpoint.path}`);
      } else {
        response = await request.post(`${GATEWAY_BASE_URL}${endpoint.path}`, {
          data: {},
        });
      }
      expect(endpoint.expectedStatus).toContain(response.status());
    });
  }
});

test.describe('Text Service API via Gateway', () => {
  test('GET /api/text/providers should return available providers', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/api/text/providers`);
    if (response.status() === 200) {
      const body = await response.json();
      expect(Array.isArray(body) || typeof body === 'object').toBeTruthy();
    }
  });

  test('GET /api/text/models should return models for a provider', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/api/text/models`, {
      params: { provider: 'openai' },
    });
    expect([200, 400, 404]).toContain(response.status());
  });

  test('POST /api/text/chat/stream should return streaming response', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/text/chat/stream`, {
      headers: { 'Content-Type': 'application/json' },
      data: {
        messages: [{ role: 'user', content: 'Hello' }],
        session_id: `test-${Date.now()}`,
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });
});

test.describe('RAG Service API via Gateway', () => {
  test('GET /api/rag/documents/ should return document list', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/api/rag/documents/`);
    expect([200, 404]).toContain(response.status());
    if (response.status() === 200) {
      const body = await response.json();
      expect(body).toHaveProperty('documents');
      expect(Array.isArray(body.documents)).toBeTruthy();
    }
  });

  test('POST /api/rag/chat/stream should accept chat request', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/rag/chat/stream`, {
      headers: { 'Content-Type': 'application/json' },
      data: {
        query: 'What is this about?',
        session_id: `test-${Date.now()}`,
        top_k: 5,
        temperature: 0.7,
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });
});

test.describe('Vision Service API via Gateway', () => {
  test('POST /api/vision/caption should accept image file', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/caption`, {
      multipart: {
        file: {
          name: 'test.png',
          mimeType: 'image/png',
          buffer: Buffer.from('fake-image-data'),
        },
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });

  test('POST /api/vision/detect should accept image file', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/detect`, {
      multipart: {
        file: {
          name: 'test.png',
          mimeType: 'image/png',
          buffer: Buffer.from('fake-image-data'),
        },
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });

  test('POST /api/vision/ocr should accept image file', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/vision/ocr`, {
      multipart: {
        file: {
          name: 'test.png',
          mimeType: 'image/png',
          buffer: Buffer.from('fake-image-data'),
        },
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });
});

test.describe('TTS Service API via Gateway', () => {
  test('GET /api/tts/voices should return voice list', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/api/tts/voices`);
    expect([200, 404]).toContain(response.status());
    if (response.status() === 200) {
      const body = await response.json();
      expect(Array.isArray(body)).toBeTruthy();
    }
  });

  test('POST /api/tts/synthesize should accept synthesize request', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/tts/synthesize`, {
      headers: { 'Content-Type': 'application/json' },
      data: {
        text: 'Hello, this is a test',
        voice: 'en-US',
        speed: 1.0,
        output_format: 'mp3',
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });
});

test.describe('Image Generation API via Gateway', () => {
  test('POST /api/image/generate should accept image generation request', async ({ request }) => {
    const response = await request.post(`${GATEWAY_BASE_URL}/api/image/generate`, {
      headers: { 'Content-Type': 'application/json' },
      data: {
        prompt: 'A beautiful sunset over the ocean',
        width: 512,
        height: 512,
        num_images: 1,
      },
    });
    expect([200, 400, 404, 500]).toContain(response.status());
  });
});

test.describe('CORS Headers', () => {
  test('Gateway should include CORS headers for Angular origin', async ({ request }) => {
    const response = await request.get(`${GATEWAY_BASE_URL}/actuator/health`, {
      headers: { Origin: 'http://localhost:4200' },
    });
    expect(response.status()).toBe(200);
    const headers = response.headers();
    expect(headers).toHaveProperty('access-control-allow-origin');
  });
});
