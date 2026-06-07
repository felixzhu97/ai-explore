---
name: angular-react-ui-test
description: Angular 与 React 迁移后 UI/交互一致性验证指南。使用 MCP 浏览器工具进行手动/半自动对比验证。对比验证 Angular 应用与 React 原版在视觉、交互、i18n 等方面的完全一致性。
globs: **/*.spec.ts **/*.test.ts **/*.cy.ts
alwaysApply: false
---

# Angular ↔ React UI/交互一致性验证规范

## 概述

本规范用于验证 Angular 迁移后的 UI 与 React 原版在视觉、交互、国际化等方面完全一致。使用 MCP 浏览器工具（`cursor-ide-browser`）进行手动/半自动对比验证，而非 Playwright 自动化测试。

---

## 核心对比维度

| 维度 | React 实现 | Angular 实现 | 验证要点 |
|------|-----------|-------------|---------|
| **布局结构** | `styled-components` / CSS-in-JS | SCSS + BEM | 容器结构、Grid/Flex 布局 |
| **颜色变量** | `theme.ts` 导出 tokens | CSS 变量 + SCSS variables | 主色、背景、边框、文字色 |
| **间距系统** | `spacing.ts` (xs/sm/md/lg/xl) | `--spacing-xs` 等 CSS 变量 | 内外边距一致 |
| **圆角** | `radius.ts` (sm/md/lg/full) | `--radius-*` CSS 变量 | 按钮、卡片、输入框圆角 |
| **字体** | `typography.ts` (fontSize/weight/family) | CSS 变量 | 字号、行高、字重 |
| **动画** | `@emotion/react` keyframes | SCSS `@keyframes` / Angular `@keyframes` | 持续时间、缓动函数一致 |
| **交互状态** | `:hover` / `:active` / `:focus-visible` | 同上 | 悬停变色、点击缩放、焦点环 |

---

## 已迁移组件对照表

### 1. AIHub (`apps/web/src/components/AIHub.tsx` → `apps/web-angular/src/app/components/ai-hub/`)

| 子区域 | React 样式 | Angular 样式 | 状态 |
|--------|-----------|-------------|------|
| SegmentedControl | `SegmentedControl.tsx` | `segmented-control/` | ✅ 已迁移 |
| Chat Area | `ChatContainer`, `MessageBubble`, `InputArea` | 需完整实现 | ⚠️ 简化版 |
| Image Generation | `ImageSection`, `PromptArea` | 缺失 | ❌ 未迁移 |
| TTS | `TTSSection`, `AudioPlayer` | 缺失 | ❌ 未迁移 |
| Model Selector | `ModelSelector` + Provider/Model select | 缺失 | ❌ 未迁移 |

**测试检查项：**
- [ ] SegmentedControl tab 切换动画一致
- [ ] Chat 输入框 placeholder 国际化
- [ ] 消息气泡对齐方向（user 右/assistant 左）
- [ ] 消息气泡圆角 (radius-lg + radius-sm)
- [ ] 加载 spinner 颜色与旋转速度
- [ ] 空状态图标与文案国际化
- [ ] Quick action buttons 样式与悬停效果

### 2. SegmentedControl (`apps/web/src/components/SegmentedControl.tsx` → `apps/web-angular/src/app/components/segmented-control/`)

| 属性 | React | Angular | 状态 |
|------|-------|---------|------|
| 容器 | `Container` (inline-flex, padding 3px) | `.container` (inline-flex, padding 3px) | ✅ 一致 |
| 选项按钮 | `Option` (padding 8px 20px) | `.option` (padding 8px 20px) | ✅ 一致 |
| 激活态 | `box-shadow: ${shadows.sm}` | `box-shadow: 0 2px 4px rgba(0,0,0,0.06)` | ⚠️ 阴影值需核对 |
| 字体 | 14px medium | 14px 500 | ✅ 一致 |
| ARIA | `role="tablist"`, `aria-selected` | 同上 | ✅ 一致 |

**测试检查项：**
- [ ] 激活选项白色背景 + 阴影
- [ ] 非激活选项透明背景 + hover 变色
- [ ] focus-visible 焦点环样式
- [ ] disabled 状态 opacity
- [ ] `box-shadow` 值完全一致

### 3. StatusBadge (`apps/web/src/components/agents/StatusBadge.tsx` → `apps/web-angular/src/app/components/agents/status-badge/`)

| 状态 | React 颜色 | Angular CSS 变量 | 状态 |
|------|-----------|-----------------|------|
| online | successLight / success | var(--color-success-light) | ✅ |
| offline | rgba(0,0,0,0.06) / textTertiary | 同上 | ✅ |
| busy | warningLight / warning | var(--color-warning-light) | ✅ |
| error | errorLight / error | var(--color-error-light) | ✅ |
| pending | primaryLight / primary | var(--color-primary-light) | ✅ |

**测试检查项：**
- [ ] Dot 圆点 (6px × 6px) + `background: currentColor`
- [ ] padding 4px 10px
- [ ] font-size xs + font-weight medium
- [ ] border-radius full (圆角胶囊)
- [ ] showDot 可控制显示/隐藏

### 4. VisionPanel (`apps/web/src/components/VisionPanel.tsx` → `apps/web-angular/src/app/components/ai/vision-panel/`)

| 功能 | React | Angular | 状态 |
|------|-------|---------|------|
| Tab 切换 | `SegmentedControl` | 内联 `.segmented-control` | ✅ |
| 图片拖放 | `onDrop` / `onDragOver` | 同上 | ✅ |
| 预览图 | `PreviewImage` (max 100%) | `.preview-image` | ✅ |
| Zoom Modal | `ImageZoomModal` | 内联 `.zoom-modal` | ⚠️ 需合并 |
| Caption 结果 | `"{{ caption }}"` | 同上 | ✅ |
| Detection 列表 | `DetectionItem` | `.detection-item` | ✅ |
| OCR 文本 | `<pre>` 格式化 | 同上 | ✅ |
| 错误消息 | `ErrorMessage` 红色背景 | `.error-message` | ⚠️ 颜色值需核对 |

**测试检查项：**
- [ ] 拖放区域虚线边框样式
- [ ] 图片悬停放大 1.02 + 阴影
- [ ] Zoom Hint 显示/隐藏动画
- [ ] ClearButton 圆形半透明黑色
- [ ] LoadingOverlay 毛玻璃效果
- [ ] Spinner 颜色与旋转速度
- [ ] 错误消息背景色 (#ffebee) / 文字色 (#c62828)

### 5. RAGChat (`apps/web/src/components/RAGChat.tsx` → `apps/web-angular/src/app/components/ai/rag-chat/`)

| 功能 | React | Angular | 状态 |
|------|-------|---------|------|
| Toast | `ToastItem` 滑入动画 | 同上 | ✅ |
| Document Card | `DocumentCard` Apple 风格 | 同上 | ✅ |
| Skeleton Loading | shimmer 动画 | shimmer 动画 | ✅ |
| 上传状态 | FileTag success/error/uploading | 同上 | ✅ |
| Chat Message | `MessageBubble` + fadeIn | 同上 | ✅ |
| Source Badge | `SourceBadge` + expand | 同上 | ✅ |
| Markdown 渲染 | react-markdown | 简化 `renderMarkdown()` | ⚠️ 功能差距 |
| 快捷问题 | QuickActions | QuickActions | ✅ |
| 输入区 | `TextArea` + `SendButton` | 同上 | ✅ |

**测试检查项：**
- [ ] Toast slideIn 动画 (translateX 100% → 0)
- [ ] Document Card 选中态背景 primary + 阴影
- [ ] Document Card 删除动画 fadeOut
- [ ] Skeleton shimmer 渐变方向 (200% → -200%)
- [ ] Message Bubble 对齐方向
- [ ] Source Panel 左侧 3px primary 色边框
- [ ] 上传按钮禁用态 opacity

### 6. ChatMessage (`apps/web/src/components/agents/ChatMessage.tsx` → `apps/web-angular/src/app/components/agents/chat-message/`)

| 功能 | React | Angular | 状态 |
|------|-------|---------|------|
| fadeIn 动画 | `@emotion/react` keyframes | Angular `@keyframes` | ✅ |
| 用户消息 | 蓝色背景 + 白色文字 | primary 背景 | ⚠️ 需核对 |
| Assistant 消息 | surface 背景 + 边框 | 同上 | ✅ |
| 代码高亮 | rehype-highlight | 简化版本 | ⚠️ 功能差距 |
| Tool Calls | `ToolResult` 组件 | 同上 | ✅ |
| 时间戳 | `MessageTime` xs 字号 | 同上 | ✅ |
| JSON 格式化 | syntax highlight | `highlightJson()` | ✅ |

**测试检查项：**
- [ ] fadeIn 持续时间 (0.2s) + translateY (8px)
- [ ] 用户消息 border-radius (lg + sm 右下)
- [ ] Assistant 消息 border-radius (lg + sm 左下)
- [ ] 代码块 background (surface-secondary)
- [ ] JSON key 紫色 / string 绿色 / number 红色

---

---

## MCP 浏览器工具验证指南

### MCP 工具简介

`cursor-ide-browser` MCP 工具提供浏览器自动化能力，适合进行 UI/交互对比验证。主要工具：

| 工具 | 用途 |
|------|------|
| `browser_navigate` | 导航到指定页面 |
| `browser_snapshot` | 获取页面结构快照（ARIA） |
| `browser_take_screenshot` | 截图（用于视觉对比） |
| `browser_click` / `browser_mouse_click_xy` | 点击元素或坐标 |
| `browser_fill` / `browser_type` | 填写表单 |
| `browser_console_messages` | 检查控制台错误 |
| `browser_network_requests` | 验证 API 请求 |

### 核心工作流程

```yaml
1. 启动服务
   - Angular: pnpm --filter @ai-test/web-angular run start  # http://localhost:4200
   - React: pnpm --filter @ai-test/web run start           # http://localhost:5173

2. 打开浏览器 Tab
   - browser_navigate → Angular 页面
   - browser_navigate → React 页面（第二个 Tab）

3. 视觉对比
   - browser_take_screenshot → 截图对比
   - 人工检查或使用图像差异工具

4. 交互验证
   - browser_snapshot → 获取页面结构
   - browser_click/browser_fill → 模拟交互
   - 验证状态变化是否符合预期

5. 控制台检查
   - browser_console_messages → 检查错误/警告
```

### MCP 工具使用示例

#### 基础导航与截图

```typescript
// 1. 打开 Angular 页面
browser_navigate({ url: "http://localhost:4200/ai-hub" })

// 2. 等待加载完成
browser_snapshot({ tab_id: "current" })  // 验证页面结构

// 3. 截图
browser_take_screenshot({
  tab_id: "current",
  take_screenshot_afterwards: true
})

// 4. 切换到 React 页面对比
browser_navigate({ url: "http://localhost:5173/ai-hub" })
browser_take_screenshot({
  tab_id: "current",
  take_screenshot_afterwards: true
})
```

#### 交互验证

```typescript
// 点击 SegmentedControl tab
browser_snapshot({ tab_id: "current" })  // 先获取结构
browser_click({ ref: "tab-button-1" })    // 点击 Vision tab

// 填写表单
browser_fill({
  ref: "chat-input",
  value: "Hello, world!"
})
browser_click({ ref: "send-button" })

// 检查结果
browser_snapshot({ tab_id: "current" })
browser_console_messages({ tab_id: "current" })  // 检查错误
```

#### 拖放文件上传

```typescript
// 对于文件上传场景，先上传文件
browser_fill({
  ref: "file-input",
  files: ["./e2e/test-image.png"]  // MCP 支持文件路径
})

// 验证预览
browser_snapshot({ tab_id: "current" })
browser_hover({ ref: "preview-image" })  // 验证悬停效果

// 截图
browser_take_screenshot({
  tab_id: "current",
  take_screenshot_afterwards: true
})
```

### 视觉一致性检查示例

#### SegmentedControl 检查

```yaml
检查项:
  - 激活 tab: 白色背景 + 阴影
  - 非激活 tab: 透明背景
  - hover: 背景色变化
  - focus-visible: 焦点环样式

验证步骤:
  1. browser_navigate → Angular VisionPanel
  2. browser_snapshot → 定位 .segment-button
  3. browser_hover → 验证悬停变色
  4. browser_click → 切换 tab
  5. browser_snapshot → 验证激活态变化
  6. browser_take_screenshot → 截图记录
```

#### StatusBadge 检查

```yaml
检查项:
  - Dot 圆点: 6px × 6px, border-radius 50%
  - 背景色: online=success / offline=gray / busy=warning / error=error / pending=primary
  - padding: 4px 10px
  - font-size: xs, font-weight: medium

验证步骤:
  1. browser_navigate → Angular AgentChat
  2. browser_snapshot → 定位 app-status-badge
  3. 检查各状态的 .badge--{status} 样式
  4. 截图对比 React 版本
```

#### VisionPanel 检查

```yaml
检查项:
  - 拖放区域: 虚线边框, 背景色 #f5f5f7
  - 预览图: max 100%, 悬停 scale(1.02) + 阴影
  - Zoom Modal: 背景 rgba(0,0,0,0.9)
  - Loading Spinner: #0071e3 蓝色, 旋转动画
  - 错误消息: 背景 #ffebee / 文字 #c62828

验证步骤:
  1. browser_navigate → Angular VisionPanel
  2. browser_fill → 上传测试图片
  3. browser_snapshot → 检查预览图显示
  4. browser_hover → 截图验证悬停效果
  5. browser_click → 打开 Zoom Modal
  6. browser_snapshot → 检查 Modal 结构
  7. browser_console_messages → 检查错误
```

#### RAGChat 检查

```yaml
检查项:
  - Toast: slideIn 动画 (translateX 100% → 0)
  - Document Card: 选中态 primary 背景
  - Skeleton: shimmer 动画 (渐变方向 200% → -200%)
  - Chat Input: border-radius 12px, focus box-shadow
  - Send Button: 禁用态 opacity 0.5

验证步骤:
  1. browser_navigate → Angular RAGChat
  2. browser_fill → 上传 PDF 文件
  3. browser_snapshot → 检查 Toast 动画
  4. browser_snapshot → 检查 Document Card
  5. browser_fill → 输入聊天内容
  6. browser_hover → 验证 Send Button 状态
  7. browser_click → 发送消息
  8. browser_snapshot → 检查消息气泡
```

### 国际化一致性检查

```yaml
检查项:
  - UI 文本是否来自 i18n（非硬编码）
  - 5 种语言切换: en / zh / ja / fr / es

验证步骤:
  1. browser_navigate → Angular 页面
  2. browser_snapshot → 检查文本内容
  3. 检查是否有硬编码英文（如 "Chat", "Generate"）
  4. 测试语言切换功能
```

### 控制台与网络检查

```yaml
检查控制台错误:
  browser_console_messages({ tab_id: "current" })
  # 检查是否有 Error 级别消息

检查网络请求:
  browser_network_requests({ tab_id: "current" })
  # 验证 API 调用是否正确
```

### 快速验证命令

```bash
# 1. 启动 Angular dev server
pnpm --filter @ai-test/web-angular run start &

# 2. 启动 React dev server
pnpm --filter @ai-test/web run start &

# 3. 使用 MCP 工具进行对比验证
#    - 打开浏览器 Tab
#    - 导航到两个版本
#    - 执行 snapshot / screenshot / click 等操作
#    - 对比结果

# 4. 记录差异
#    - 截图保存到 docs/screenshots/
#    - 记录不一致项到 GitHub Issue
```

### MCP 工具优势

| 对比项 | Playwright E2E | MCP 浏览器工具 |
|--------|----------------|----------------|
| **交互方式** | 自动化脚本 | 人工 + 半自动 |
| **适用场景** | CI/CD 回归测试 | 开发时实时验证 |
| **截图对比** | 需要额外图像处理 | 直接截图对比 |
| **灵活性** | 修改测试代码 | 实时调整验证步骤 |
| **调试** | 日志输出 | 可视化操作 |

> **注意**：MCP 工具适合开发过程中的实时验证。对于持续集成，仍可保留 Playwright 测试，但验证流程应以 MCP 为主。

---

## 已知差距与修复优先级

| 优先级 | 组件 | 差距描述 | 修复方式 |
|--------|------|---------|---------|
| 🔴 高 | AIHub | Chat/Image/TTS 子区域未完整实现 | 补充 VisionPanel、RAGChat、TTS 功能 |
| 🔴 高 | VisionPanel | Zoom Modal 未提取为独立组件 | 复用 `image-zoom-modal/` |
| 🟡 中 | RAGChat | Markdown 渲染功能简化 | 集成 `marked` + `highlight.js` |
| 🟡 中 | ChatMessage | 代码高亮功能简化 | 集成 `highlight.js` |
| 🟢 低 | i18n | 部分组件仍有硬编码文本 | 统一使用 `I18nService` |

---

## 视觉差异快速排查清单

当发现 Angular 与 React 渲染不一致时，按以下顺序排查：

1. **颜色值**：检查 `theme.ts` vs CSS 变量是否完全对应
2. **间距**：检查 `spacing.ts` 的 rem/px 值
3. **圆角**：检查 `radius.ts` 的数值
4. **动画时长**：检查 `transitions` 对象的时长
5. **字体**：检查 `typography.ts` 的 fontFamily 顺序
6. **阴影**：检查 `shadows.ts` 的 rgba 值
7. **z-index**：检查模态框、Toast 的层级
8. **BEM 嵌套**：Angular SCSS 可能存在选择器优先级差异
