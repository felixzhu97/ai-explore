import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? undefined : 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'test-results/playwright-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
  ],

  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    outputDir: 'test-results',
    viewport: { width: 1280, height: 720 },
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], headless: !!process.env.CI },
    },
    {
      name: 'chromium-mobile',
      use: { ...devices['Pixel 5'], headless: true },
    },
  ],

  webServer: {
    command: 'npm run docker:up && npm run java:dev && npm run angular:dev',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env.CI,
    timeout: 300 * 1000,
    stdout: 'ignore',
    stderr: 'pipe',
  },

  timeout: 60000,
  expect: {
    timeout: 10000,
  },
});
