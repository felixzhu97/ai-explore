import { Page, Locator, FileChooser } from '@playwright/test';
import { BasePage } from './base.page';

export class VisionPage extends BasePage {
  readonly tabControl: Locator;
  readonly imageArea: Locator;
  readonly uploadInput: Locator;
  readonly analyzeButton: Locator;
  readonly resultPanel: Locator;
  readonly clearButton: Locator;

  constructor(page: Page) {
    super(page, '/vision');
    this.tabControl = page.locator('app-segmented-control');
    this.imageArea = page.locator('.image-area');
    this.uploadInput = page.locator('input[type="file"]');
    this.analyzeButton = page.locator('button:has-text("Analyze"), button:has-text("分析")');
    this.resultPanel = page.locator('.result-content');
    this.clearButton = this.imageArea.locator('.clear-button');
  }

  async selectTask(task: 'caption' | 'detect' | 'ocr'): Promise<void> {
    const taskLabels: Record<string, string> = {
      caption: /caption/i,
      detect: /detect|object/i,
      ocr: /ocr|text/i,
    };
    await this.tabControl.locator(`button:has-text("${task}")`).click();
    await this.page.waitForTimeout(300);
  }

  async uploadImage(filePath: string): Promise<void> {
    const [fileChooser] = await Promise.all([
      this.page.waitForEvent('filechooser'),
      this.uploadInput.first().click(),
    ]);
    await fileChooser.setFiles(filePath);
  }

  async analyzeImage(): Promise<void> {
    await this.analyzeButton.click();
  }

  async waitForResult(timeout = 30000): Promise<void> {
    await this.page.waitForSelector('.result-text, .detection-list, .ocr-text', { timeout });
  }

  async clearImage(): Promise<void> {
    await this.clearButton.click();
  }

  async isImagePreviewVisible(): Promise<boolean> {
    return this.imageArea.locator('.preview-image').isVisible();
  }

  async getResult(): Promise<string> {
    const result = this.resultPanel.locator('.result-text, .ocr-text');
    return result.textContent() ?? '';
  }

  async getDetectionCount(): Promise<number> {
    return this.resultPanel.locator('.detection-item').count();
  }
}
