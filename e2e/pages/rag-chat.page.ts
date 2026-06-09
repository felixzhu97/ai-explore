import { Page, Locator, FileChooser } from '@playwright/test';
import { BasePage } from './base.page';

export class RagChatPage extends BasePage {
  readonly chatContainer: Locator;
  readonly chatInput: Locator;
  readonly sendButton: Locator;
  readonly fileUpload: Locator;
  readonly documentsSection: Locator;
  readonly emptyState: Locator;

  constructor(page: Page) {
    super(page, '/rag');
    this.chatContainer = page.locator('.chat-container');
    this.chatInput = page.locator('.chat-input, textarea');
    this.sendButton = page.locator('.send-button, button:has-text("→")');
    this.fileUpload = page.locator('input[type="file"]#file-upload');
    this.documentsSection = page.locator('.documents-section');
    this.emptyState = page.locator('.empty-state');
  }

  async sendMessage(message: string): Promise<void> {
    await this.chatInput.fill(message);
    await this.sendButton.click();
  }

  async waitForResponse(timeout = 30000): Promise<void> {
    await this.page.waitForSelector('.message-bubble:not(.user)', { timeout });
  }

  async getMessageCount(): Promise<number> {
    return this.chatContainer.locator('.message-bubble').count();
  }

  async uploadFile(filePath: string): Promise<void> {
    const [fileChooser] = await Promise.all([
      this.page.waitForEvent('filechooser'),
      this.fileUpload.click(),
    ]);
    await fileChooser.setFiles(filePath);
  }

  async isDocumentsSectionVisible(): Promise<boolean> {
    return this.documentsSection.isVisible();
  }

  async selectAllDocuments(): Promise<void> {
    const selectAllBtn = this.page.locator('button:has-text("Select All"), button:has-text("全选")');
    if (await selectAllBtn.isVisible()) {
      await selectAllBtn.click();
    }
  }

  async getQuickActions(): Promise<string[]> {
    const buttons = this.page.locator('.quick-action');
    const count = await buttons.count();
    const texts: string[] = [];
    for (let i = 0; i < count; i++) {
      texts.push(await buttons.nth(i).textContent() ?? '');
    }
    return texts;
  }
}
