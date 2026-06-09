import { Page, Locator } from '@playwright/test';
import { BasePage } from './base.page';

export class AiInfraPage extends BasePage {
  readonly tabControl: Locator;
  readonly agentPanel: Locator;
  readonly chatInput: Locator;
  readonly sendButton: Locator;
  readonly chatContainer: Locator;

  constructor(page: Page) {
    super(page, '/ai-infra');
    this.tabControl = page.locator('app-segmented-control');
    this.agentPanel = page.locator('app-agent-panel');
    this.chatInput = page.locator('.chat-input, textarea');
    this.sendButton = page.locator('.send-button, button:has-text("→")');
    this.chatContainer = page.locator('.chat-container, .messages');
  }

  async selectTab(tabName: string): Promise<void> {
    await this.tabControl.locator(`button:has-text("${tabName}")`).click();
    await this.page.waitForTimeout(300);
  }

  async sendMessage(message: string): Promise<void> {
    await this.chatInput.fill(message);
    await this.sendButton.click();
  }

  async waitForResponse(timeout = 30000): Promise<void> {
    await this.page.waitForSelector('.message-bubble:not(.user)', { timeout });
  }

  async getMessages(): Promise<Locator[]> {
    return this.chatContainer.locator('.message-bubble').all();
  }

  async getLastAssistantMessage(): Promise<string> {
    const messages = this.chatContainer.locator('.message-bubble:not(.user)');
    const count = await messages.count();
    if (count === 0) return '';
    return messages.nth(count - 1).textContent() ?? '';
  }
}
