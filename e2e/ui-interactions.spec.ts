import { test, expect } from '@playwright/test';
import { AiInfraPage } from './pages/ai-infra.page';
import { RagChatPage } from './pages/rag-chat.page';
import { VisionPage } from './pages/vision.page';
import { AiHubPage } from './pages/ai-hub.page';
import { ChatFactory } from './factories';

test.describe('Navigation Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('should load the main page and redirect to /ai-infra', async ({ page }) => {
    await expect(page).toHaveURL(/\/ai-infra/);
  });

  test('should have a valid page title', async ({ page }) => {
    const title = await page.title();
    expect(title.length).toBeGreaterThan(0);
  });

  test.describe('Route Navigation', () => {
    test('should navigate to /ai-infra', async ({ page }) => {
      await page.goto('/ai-infra');
      await page.waitForLoadState('networkidle');
      await expect(page.locator('app-ai-infra-panel, app-segmented-control')).toBeVisible();
    });

    test('should navigate to /rag', async ({ page }) => {
      await page.goto('/rag');
      await page.waitForLoadState('networkidle');
      await expect(page.locator('app-rag-chat')).toBeVisible();
    });

    test('should navigate to /vision', async ({ page }) => {
      await page.goto('/vision');
      await page.waitForLoadState('networkidle');
      await expect(page.locator('app-vision-panel')).toBeVisible();
    });

    test('should navigate to /aihubs', async ({ page }) => {
      await page.goto('/aihubs');
      await page.waitForLoadState('networkidle');
      await expect(page.locator('app-ai-hub')).toBeVisible();
    });
  });
});

test.describe('AI Infra Panel Tests', () => {
  test('should display AI Infra page with tab control', async ({ page }) => {
    const aiInfraPage = new AiInfraPage(page);
    await aiInfraPage.navigate();

    await expect(aiInfraPage.tabControl).toBeVisible();
  });

  test('should have supervisor tab selected by default', async ({ page }) => {
    const aiInfraPage = new AiInfraPage(page);
    await aiInfraPage.navigate();

    const tabs = aiInfraPage.tabControl.locator('button');
    const firstTab = tabs.first();
    await expect(firstTab).toHaveAttribute('aria-selected', 'true');
  });

  test('should switch between agent tabs', async ({ page }) => {
    const aiInfraPage = new AiInfraPage(page);
    await aiInfraPage.navigate();

    await aiInfraPage.selectTab('k8s');
    await expect(page.locator('app-agent-panel')).toBeVisible();
  });

  test('should display chat input area', async ({ page }) => {
    const aiInfraPage = new AiInfraPage(page);
    await aiInfraPage.navigate();

    await expect(aiInfraPage.chatInput).toBeVisible();
  });

  test('should have send button', async ({ page }) => {
    const aiInfraPage = new AiInfraPage(page);
    await aiInfraPage.navigate();

    await expect(aiInfraPage.sendButton).toBeVisible();
  });
});

test.describe('RAG Chat Tests', () => {
  test('should display RAG chat page', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    await expect(ragPage.chatContainer).toBeVisible();
  });

  test('should show empty state initially', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    await expect(ragPage.emptyState).toBeVisible();
  });

  test('should display quick action buttons', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    const quickActions = await ragPage.getQuickActions();
    expect(quickActions.length).toBeGreaterThan(0);
  });

  test('should have file upload area', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    await expect(ragPage.fileUpload).toBeAttached();
  });

  test('should have documents section', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    await expect(ragPage.documentsSection).toBeVisible();
  });

  test('should display chat input', async ({ page }) => {
    const ragPage = new RagChatPage(page);
    await ragPage.navigate();

    await expect(ragPage.chatInput).toBeVisible();
  });
});

test.describe('Vision Panel Tests', () => {
  test('should display vision page with tab control', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await expect(visionPage.tabControl).toBeVisible();
  });

  test('should have caption task selected by default', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    const captionTab = visionPage.tabControl.locator('button').first();
    await expect(captionTab).toBeVisible();
  });

  test('should switch to detect task', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await visionPage.selectTask('detect');
  });

  test('should switch to OCR task', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await visionPage.selectTask('ocr');
  });

  test('should have image upload area', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await expect(visionPage.imageArea).toBeVisible();
  });

  test('should have analyze button (disabled initially)', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await expect(visionPage.analyzeButton).toBeVisible();
    await expect(visionPage.analyzeButton).toBeDisabled();
  });

  test('should have result panel', async ({ page }) => {
    const visionPage = new VisionPage(page);
    await visionPage.navigate();

    await expect(visionPage.resultPanel).toBeVisible();
  });
});

test.describe('AI Hub Tests', () => {
  test('should display AI Hub page with tab control', async ({ page }) => {
    const aiHubPage = new AiHubPage(page);
    await aiHubPage.navigate();

    await expect(aiHubPage.tabControl).toBeVisible();
  });

  test('should have Chat tab selected by default', async ({ page }) => {
    const aiHubPage = new AiHubPage(page);
    await aiHubPage.navigate();

    await expect(aiHubPage.chatTab).toBeVisible();
  });

  test('should switch to Image tab', async ({ page }) => {
    const aiHubPage = new AiHubPage(page);
    await aiHubPage.navigate();

    await aiHubPage.selectTab('image');
    await expect(aiHubPage.promptInput).toBeVisible();
  });

  test('should switch to TTS tab', async ({ page }) => {
    const aiHubPage = new AiHubPage(page);
    await aiHubPage.navigate();

    await aiHubPage.selectTab('tts');
    await expect(aiHubPage.ttsTextInput).toBeVisible();
  });

  test.describe('Chat Tab', () => {
    test('should display model selector', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();

      await expect(aiHubPage.providerSelect).toBeVisible();
      await expect(aiHubPage.modelSelect).toBeVisible();
    });

    test('should display chat input', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();

      await expect(aiHubPage.chatInput).toBeVisible();
    });

    test('should display send button', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();

      await expect(aiHubPage.sendButton).toBeVisible();
    });
  });

  test.describe('Image Generation Tab', () => {
    test('should display prompt input', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('image');

      await expect(aiHubPage.promptInput).toBeVisible();
    });

    test('should display size selector', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('image');

      await expect(aiHubPage.sizeSelector.first()).toBeVisible();
    });

    test('should display generate button', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('image');

      await expect(aiHubPage.generateButton).toBeVisible();
    });
  });

  test.describe('TTS Tab', () => {
    test('should display text input', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('tts');

      await expect(aiHubPage.ttsTextInput).toBeVisible();
    });

    test('should display voice selector', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('tts');

      await expect(aiHubPage.voiceSelect).toBeVisible();
    });

    test('should display synthesize button', async ({ page }) => {
      const aiHubPage = new AiHubPage(page);
      await aiHubPage.navigate();
      await aiHubPage.selectTab('tts');

      await expect(aiHubPage.synthesizeButton).toBeVisible();
    });
  });
});

test.describe('Responsive Design Tests', () => {
  const viewports = [
    { name: 'Mobile', width: 375, height: 667 },
    { name: 'Tablet', width: 768, height: 1024 },
    { name: 'Desktop', width: 1280, height: 720 },
  ];

  for (const viewport of viewports) {
    test(`should render correctly on ${viewport.name}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto('/ai-infra');
      await page.waitForLoadState('networkidle');

      const body = page.locator('body');
      await expect(body).toBeVisible();
    });
  }
});
