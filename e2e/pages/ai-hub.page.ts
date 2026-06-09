import { Page, Locator } from '@playwright/test';
import { BasePage } from './base.page';

export class AiHubPage extends BasePage {
  readonly tabControl: Locator;
  readonly chatTab: Locator;
  readonly imageTab: Locator;
  readonly ttsTab: Locator;

  // Chat elements
  readonly chatInput: Locator;
  readonly sendButton: Locator;
  readonly chatContainer: Locator;
  readonly providerSelect: Locator;
  readonly modelSelect: Locator;

  // Image generation elements
  readonly promptInput: Locator;
  readonly negativePromptInput: Locator;
  readonly generateButton: Locator;
  readonly generatedImage: Locator;
  readonly sizeSelector: Locator;

  // TTS elements
  readonly ttsTextInput: Locator;
  readonly voiceSelect: Locator;
  readonly speedSlider: Locator;
  readonly synthesizeButton: Locator;
  readonly audioPlayer: Locator;

  constructor(page: Page) {
    super(page, '/aihubs');
    this.tabControl = page.locator('app-segmented-control');

    // Tabs
    this.chatTab = page.locator('button:has-text("Chat"), button:has-text("聊天")').first();
    this.imageTab = page.locator('button:has-text("Image"), button:has-text("图像")').first();
    this.ttsTab = page.locator('button:has-text("TTS"), button:has-text("语音")').first();

    // Chat
    this.chatInput = page.locator('.chat-input, textarea').first();
    this.sendButton = page.locator('.send-button').first();
    this.chatContainer = page.locator('.chat-container').first();
    this.providerSelect = page.locator('.model-select').first();
    this.modelSelect = page.locator('.model-select').nth(1);

    // Image
    this.promptInput = page.locator('textarea').first();
    this.negativePromptInput = page.locator('textarea').nth(1);
    this.generateButton = page.locator('button:has-text("Generate"), button:has-text("生成")').first();
    this.generatedImage = page.locator('.generated-image');
    this.sizeSelector = page.locator('.size-option');

    // TTS
    this.ttsTextInput = page.locator('.text-input, textarea').first();
    this.voiceSelect = page.locator('.text-select, select').first();
    this.speedSlider = page.locator('.slider');
    this.synthesizeButton = page.locator('button:has-text("Synthesize"), button:has-text("合成")').first();
    this.audioPlayer = page.locator('.audio-player');
  }

  async selectTab(tab: 'chat' | 'image' | 'tts'): Promise<void> {
    switch (tab) {
      case 'chat':
        await this.chatTab.click();
        break;
      case 'image':
        await this.imageTab.click();
        break;
      case 'tts':
        await this.ttsTab.click();
        break;
    }
    await this.page.waitForTimeout(300);
  }

  // Chat methods
  async sendChatMessage(message: string): Promise<void> {
    await this.chatInput.fill(message);
    await this.sendButton.click();
  }

  async selectProvider(provider: string): Promise<void> {
    await this.providerSelect.selectOption(provider);
  }

  async selectModel(model: string): Promise<void> {
    await this.modelSelect.selectOption(model);
  }

  async waitForChatResponse(timeout = 30000): Promise<void> {
    await this.page.waitForSelector('.message-bubble:not(.user)', { timeout });
  }

  async getLastAssistantResponse(): Promise<string> {
    const messages = this.chatContainer.locator('.message-bubble:not(.user)');
    const count = await messages.count();
    if (count === 0) return '';
    return messages.nth(count - 1).textContent() ?? '';
  }

  // Image generation methods
  async fillPrompt(prompt: string): Promise<void> {
    await this.promptInput.fill(prompt);
  }

  async fillNegativePrompt(prompt: string): Promise<void> {
    await this.negativePromptInput.fill(prompt);
  }

  async selectImageSize(sizeLabel: string): Promise<void> {
    await this.sizeSelector.locator(`button:has-text("${sizeLabel}")`).click();
  }

  async generateImage(): Promise<void> {
    await this.generateButton.click();
  }

  async waitForGeneratedImage(timeout = 60000): Promise<void> {
    await this.generatedImage.waitFor({ state: 'visible', timeout });
  }

  async downloadImage(): Promise<void> {
    const downloadBtn = this.page.locator('button:has-text("Download"), button:has-text("下载")');
    await downloadBtn.click();
  }

  // TTS methods
  async fillTtsText(text: string): Promise<void> {
    await this.ttsTextInput.fill(text);
  }

  async selectVoice(voiceId: string): Promise<void> {
    await this.voiceSelect.selectOption(voiceId);
  }

  async setSpeed(speed: number): Promise<void> {
    await this.speedSlider.fill(speed.toString());
  }

  async synthesize(): Promise<void> {
    await this.synthesizeButton.click();
  }

  async waitForAudioPlayer(timeout = 30000): Promise<void> {
    await this.audioPlayer.waitFor({ state: 'visible', timeout });
  }

  async isAudioPlayerVisible(): Promise<boolean> {
    return this.audioPlayer.isVisible();
  }
}
