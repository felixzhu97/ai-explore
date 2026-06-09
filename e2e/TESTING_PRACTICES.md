# Playwright E2E Testing Best Practices

## Overview

This document outlines best practices for writing maintainable, reliable, and scalable E2E tests using Playwright for the Angular frontend and Java backend (Gateway) application.

## Project Structure

```
e2e/
├── pages/                    # Page Object Model classes
│   ├── base.page.ts         # Base page with common methods
│   ├── ai-infra.page.ts     # AI Infrastructure page
│   ├── rag-chat.page.ts     # RAG Chat page
│   ├── vision.page.ts       # Vision panel page
│   ├── ai-hub.page.ts      # AI Hub page
│   └── index.ts             # Export all pages
├── factories/               # Test data factories
│   ├── test-data.factory.ts # Factory methods for test data
│   └── index.ts
├── gateway-api.spec.ts      # Gateway API tests
├── api-integration.spec.ts   # API integration tests
├── ui-interactions.spec.ts  # UI interaction tests
├── health-check.spec.ts     # Health check tests
├── playwright.config.ts      # Playwright configuration
└── package.json             # Test scripts
```

## Naming Conventions

### Test Files

- **API Tests**: `<feature>-api.spec.ts`
  - Example: `gateway-api.spec.ts`, `chat-api.spec.ts`
- **UI Tests**: `<feature>-interactions.spec.ts`
  - Example: `ui-interactions.spec.ts`
- **Integration Tests**: `<feature>-integration.spec.ts`
  - Example: `api-integration.spec.ts`

### Test Functions

Use descriptive names that explain the expected behavior:

```typescript
// Good
test('should display user profile after login')
test('should submit form with valid data')
test('should show error message for invalid email')

// Bad
test('login test')
test('form test')
test('email error')
```

### Page Object Methods

```typescript
// Good - descriptive action names
async loginAs(user: User): Promise<void>
async submitSearchQuery(query: string): Promise<void>
async waitForLoadingIndicator(): Promise<void>

// Bad - generic names
async click(): Promise<void>
async doAction(): Promise<void>
```

## Test Organization

### Using test.describe()

Group related tests using `test.describe()`:

```typescript
test.describe('User Authentication', () => {
  test.describe('Login', () => {
    test('should login with valid credentials');
    test('should show error for invalid password');
    test('should redirect after successful login');
  });

  test.describe('Logout', () => {
    test('should clear session on logout');
    test('should redirect to home page');
  });
});
```

### Test Tags with test.describe()

Use `test.describe()` with tags for test organization:

```typescript
test.describe('smoke', () => {
  test('critical path test 1');
  test('critical path test 2');
});

test.describe('regression', () => {
  test('detailed feature test');
});
```

Run specific groups: `npx playwright test --grep="smoke"`

## Page Object Model (POM)

### Base Page Pattern

```typescript
export class BasePage {
  constructor(protected page: Page) {}

  async navigate(path = '/'): Promise<void> {
    await this.page.goto(path);
    await this.page.waitForLoadState('networkidle');
  }

  async isVisible(selector: string): Promise<boolean> {
    return this.page.locator(selector).isVisible();
  }

  async click(selector: string): Promise<void> {
    await this.page.locator(selector).click();
  }

  async fill(selector: string, value: string): Promise<void> {
    await this.page.locator(selector).fill(value);
  }
}
```

### Custom Page Classes

```typescript
export class ChatPage extends BasePage {
  readonly messageInput = this.page.locator('.chat-input');
  readonly sendButton = this.page.locator('.send-button');
  readonly messages = this.page.locator('.message-bubble');

  async sendMessage(message: string): Promise<void> {
    await this.messageInput.fill(message);
    await this.sendButton.click();
  }

  async getLastMessage(): Promise<string> {
    const count = await this.messages.count();
    return this.messages.nth(count - 1).textContent() ?? '';
  }
}
```

## Test Data Factories

### Factory Pattern

```typescript
export class ChatFactory {
  static createMessage(role: 'user' | 'assistant', content: string) {
    return {
      id: `msg_${Date.now()}`,
      role,
      content,
      timestamp: Date.now(),
    };
  }

  static createUserMessage(content: string) {
    return this.createMessage('user', content);
  }

  static createAssistantMessage(content: string) {
    return this.createMessage('assistant', content);
  }
}
```

### Usage in Tests

```typescript
test('should display user message', async ({ page }) => {
  const userMessage = ChatFactory.createUserMessage('Hello');
  // Use in test assertions
});
```

## Waiting Strategies

### Prefer Built-in Waits

```typescript
// Good - Playwright auto-waits
await page.click('#submit');
await page.fill('#input', 'value');

// Good - explicit wait for state
await expect(page.locator('#result')).toBeVisible({ timeout: 10000 });

// Bad - arbitrary sleep
await page.waitForTimeout(5000);
```

### Waiting for Network Requests

```typescript
// Wait for specific API call
await page.waitForResponse(
  response => response.url().includes('/api/text/chat') && response.status() === 200
);

// Wait for navigation
await page.waitForURL('**/success');
```

## Assertions

### Use Playwright Assertions

```typescript
import { expect } from '@playwright/test';

// Basic assertions
await expect(page.locator('#title')).toHaveText('Expected Title');
await expect(page.locator('#error')).toBeVisible();
await expect(page.locator('#loading')).toBeHidden();

// Complex assertions
await expect(page).toHaveURL(/\/success/);
await expect(page).toHaveTitle(/Dashboard/);
await expect(page.locator('.user')).toHaveCount(5);
```

### Custom Assertions

```typescript
expect.extend({
  async toHaveNoConsoleErrors(receiver: Page) {
    const errors: string[] = [];
    receiver.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    // ... assertion logic
  },
});
```

## Parallel Execution

### CI Mode Configuration

```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: !!process.env.CI,
  workers: process.env.CI ? undefined : 1,
  retries: process.env.CI ? 2 : 0,
});
```

### Test Isolation

```typescript
test('isolated test', async ({ page, context }) => {
  // Each test gets fresh context
  const newPage = await context.newPage();
  // Test isolation
});
```

## Screenshots and Videos

### Capture on Failure

```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
});
```

### Manual Capture

```typescript
test('debug test', async ({ page }) => {
  await page.goto('/');
  await page.screenshot({ path: 'debug.png', fullPage: true });
});
```

## Debugging

### Debug Mode

```bash
npx playwright test --debug
npx playwright test --project=chromium --headed
```

### UI Mode

```bash
npx playwright test --ui
```

### Codegen

```bash
npx playwright codegen http://localhost:4200
```

## Running Tests

### Run All Tests

```bash
pnpm test:e2e
```

### Run Specific Test File

```bash
pnpm playwright test gateway-api.spec.ts
```

### Run Tests by Pattern

```bash
pnpm playwright test --grep="smoke"
pnpm playwright test --grep="API"
```

### Run Tests by Tags

```bash
pnpm playwright test --grep="@slow"
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: E2E Tests
on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v3
      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'pnpm'

      - name: Install dependencies
        run: pnpm install

      - name: Install Playwright
        run: pnpm test:e2e:install

      - name: Build application
        run: pnpm build

      - name: Run E2E tests
        run: pnpm test:e2e:ci
        env:
          CI: true
```

## Anti-Patterns to Avoid

### 1. Don't Use Arbitrary Waits

```typescript
// Bad
await page.waitForTimeout(5000);

// Good
await page.waitForSelector('#element', { state: 'visible' });
```

### 2. Don't Hardcode Sleep in Tests

```typescript
// Bad
await page.click('#submit');
await page.waitForTimeout(1000);
expect(await page.locator('#result').isVisible()).toBe(true);

// Good
await page.click('#submit');
await expect(page.locator('#result')).toBeVisible();
```

### 3. Don't Test Implementation Details

```typescript
// Bad - testing internal state
expect(await page.evaluate(() => window.appState.user)).toBe('John');

// Good - testing visible behavior
await expect(page.locator('.user-name')).toHaveText('John');
```

### 4. Don't Create Brittle Selectors

```typescript
// Bad - position-based selector
await page.locator('div:nth-child(3) > span').click();

// Good - semantic selector
await page.getByRole('button', { name: 'Submit' }).click();
```

## Best Practices Summary

1. **Use Page Object Model**: Encapsulate page interactions
2. **Use Test Factories**: Create reusable test data
3. **Prefer Built-in Waits**: Let Playwright handle timing
4. **Write Descriptive Tests**: Clear intent in test names
5. **Group Related Tests**: Use `test.describe()` for organization
6. **Isolate Tests**: Each test should be independent
7. **Capture Evidence**: Screenshots/videos on failure
8. **Use Semantic Selectors**: Prefer `getByRole`, `getByText`
9. **Don't Test Implementation**: Focus on user-visible behavior
10. **Run in CI**: Automate test execution

## Resources

- [Playwright Documentation](https://playwright.dev/docs/intro)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Testing Library Guiding Principles](https://testing-library.com/docs/guiding-principles)
