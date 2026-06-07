# UI/交互对比验证 - Subagent 提示词

## 概述

本提示词用于指导 subagent 使用 MCP 浏览器工具（`cursor-ide-browser`）进行 Angular 与 React 应用的 UI/交互一致性对比验证。

## 标准验证流程

### 1. 准备工作

```yaml
前置条件:
  - Angular 服务运行中: http://localhost:4200
  - React 服务运行中: http://localhost:5173
  - 测试文件准备: e2e/test-image.png, e2e/test.pdf

验证顺序:
  1. 先验证 React 版本（参考实现）
  2. 再验证 Angular 版本（待验证实现）
  3. 对比差异
```

### 2. MCP 工具使用顺序

```yaml
基础操作:
  1. browser_tabs → 列出当前 Tab
  2. browser_navigate → 导航到目标页面
  3. browser_snapshot → 获取页面结构
  4. browser_take_screenshot → 截图记录

交互操作:
  5. browser_click → 点击元素
  6. browser_fill/browser_type → 填写表单
  7. browser_hover → 悬停验证
  8. browser_scroll → 滚动页面

检查操作:
  9. browser_console_messages → 控制台错误
  10. browser_network_requests → 网络请求
```

### 3. 检查清单模板

#### SegmentedControl 检查清单

```yaml
✅ 视觉检查:
  - [ ] 容器布局: inline-flex, padding 3px
  - [ ] 选项按钮: padding 8px 20px
  - [ ] 激活态: 白色背景 + box-shadow
  - [ ] 非激活态: 透明背景
  - [ ] 字体: 14px medium
  - [ ] focus-visible: 焦点环样式

✅ 交互检查:
  - [ ] 点击切换激活态
  - [ ] 键盘导航 (Tab/Arrow)
  - [ ] 切换动画平滑

✅ ARIA 检查:
  - [ ] role="tablist"
  - [ ] aria-selected 状态正确
  - [ ] aria-controls 关联内容

验证命令:
  browser_snapshot → 定位 .segment-button
  browser_hover → 验证悬停效果
  browser_click → 切换 tab
```

#### StatusBadge 检查清单

```yaml
✅ 视觉检查:
  - [ ] Dot 圆点: 6px × 6px, border-radius 50%
  - [ ] online 状态: success 色 (#34c759)
  - [ ] offline 状态: gray 色
  - [ ] busy 状态: warning 色 (#ffcc00)
  - [ ] error 状态: error 色 (#ff3b30)
  - [ ] pending 状态: primary 色 (#007aff)
  - [ ] padding: 4px 10px
  - [ ] font-size: xs, font-weight: medium
  - [ ] border-radius: full (圆角胶囊)

✅ 功能检查:
  - [ ] showDot 控制显示/隐藏
  - [ ] 状态切换正确反映颜色变化

验证命令:
  browser_snapshot → 定位 app-status-badge
  browser_take_screenshot → 截图对比
```

#### VisionPanel 检查清单

```yaml
✅ 拖放区域检查:
  - [ ] 背景色: #f5f5f7
  - [ ] 虚线边框样式
  - [ ] 拖放提示图标/文字

✅ 图片预览检查:
  - [ ] 预览图 max 100%
  - [ ] 悬停: scale(1.02) + 阴影
  - [ ] Zoom Hint 显示/隐藏动画

✅ Zoom Modal 检查:
  - [ ] 背景色: rgba(0,0,0,0.9)
  - [ ] 关闭按钮样式
  - [ ] 点击背景关闭

✅ Loading 检查:
  - [ ] Spinner 颜色: #0071e3
  - [ ] 旋转动画流畅

✅ 错误消息检查:
  - [ ] 背景色: #ffebee
  - [ ] 文字色: #c62828

✅ 国际化检查:
  - [ ] Tab labels 来自 i18n
  - [ ] 按钮文字来自 i18n

验证命令:
  browser_fill → 上传测试图片
  browser_hover → 验证悬停效果
  browser_click → 打开 Zoom Modal
  browser_console_messages → 检查错误
```

#### RAGChat 检查清单

```yaml
✅ Toast 检查:
  - [ ] slideIn 动画: translateX 100% → 0
  - [ ] 位置: 右上角
  - [ ] 自动消失时间

✅ Document Card 检查:
  - [ ] Apple 风格卡片样式
  - [ ] 选中态: primary 背景 + 阴影
  - [ ] 删除动画: fadeOut

✅ Skeleton Loading 检查:
  - [ ] shimmer 动画
  - [ ] 渐变方向: 200% → -200%

✅ Chat Input 检查:
  - [ ] border-radius: 12px
  - [ ] focus: box-shadow rgba(0,113,227,0.1)

✅ Send Button 检查:
  - [ ] 禁用态: opacity 0.5
  - [ ] 启用态: 点击发送

✅ 消息气泡检查:
  - [ ] 用户消息: 右对齐, primary 背景
  - [ ] Assistant 消息: 左对齐, surface 背景
  - [ ] fadeIn 动画: translateY 8px, 0.2s

✅ Source Panel 检查:
  - [ ] 左侧 3px primary 色边框

验证命令:
  browser_fill → 上传 PDF 文件
  browser_snapshot → 检查 Toast/Card
  browser_type → 输入聊天内容
  browser_click → 发送消息
```

### 4. 截图对比方法

```yaml
对比策略:
  1. 同时打开两个 Tab（React + Angular）
  2. 切换 Tab 截图对比
  3. 使用图像差异工具（如 Pixelmatch）

截图命名规范:
  {component}-{state}-{version}.png
  例如:
    - vision-panel-upload-react.png
    - vision-panel-upload-angular.png
    - vision-panel-zoom-react.png
    - vision-panel-zoom-angular.png

截图保存位置:
  docs/screenshots/ai-hub/
  docs/screenshots/vision-panel/
  docs/screenshots/rag-chat/
```

### 5. 差异记录格式

```markdown
## {组件名} UI 差异报告

### 验证日期
{YYYY-MM-DD}

### 验证人
{subagent/agent name}

### 环境
- Angular: http://localhost:4200
- React: http://localhost:5173

### 差异清单

| 差异项 | React 实现 | Angular 实现 | 严重程度 | 修复建议 |
|--------|-----------|-------------|---------|---------|
| {描述} | {React 样式} | {Angular 样式} | 🔴高/🟡中/🟢低 | {建议} |

### 视觉证据
- [截图1: React 版本]
- [截图2: Angular 版本]
- [截图3: 差异标注]

### 结论
{通过/需修复/需进一步确认}
```

### 6. 验证完成报告格式

```markdown
# UI/交互对比验证报告

## 验证概览

| 组件 | 验证状态 | 差异数 | 阻塞问题 |
|------|---------|--------|---------|
| SegmentedControl | ✅ 通过 | 0 | 无 |
| StatusBadge | ✅ 通过 | 0 | 无 |
| VisionPanel | ⚠️ 部分通过 | 2 | Zoom Modal |
| RAGChat | ❌ 未通过 | 5 | Markdown 渲染 |

## 详细结果

### SegmentedControl ✅
- [x] 视觉一致
- [x] 交互一致
- [x] ARIA 正确

### VisionPanel ⚠️
- [x] 拖放区域样式一致
- [x] 预览图样式一致
- [ ] Zoom Modal 未实现 → 阻塞功能
- [ ] 错误消息颜色差异 → 需修复

### RAGChat ❌
- [ ] Markdown 渲染简化 → 功能缺失
- [ ] 代码高亮缺失 → 功能缺失
- [ ] Toast 动画不一致 → 需修复

## 下一步行动

### 高优先级
1. 实现 VisionPanel Zoom Modal（阻塞）
2. 集成 RAGChat Markdown 渲染（阻塞）

### 中优先级
1. 修复 Toast 动画差异
2. 修复错误消息颜色

### 低优先级
1. 优化代码高亮效果
```

## MCP 工具速查表

| 工具 | 参数 | 用途 |
|------|------|------|
| `browser_navigate` | `url` | 导航到页面 |
| `browser_tabs` | `action: "list"` | 列出所有 Tab |
| `browser_tabs` | `action: "new"` | 新建 Tab |
| `browser_snapshot` | `tab_id`, `take_screenshot_afterwards` | 获取页面结构 |
| `browser_take_screenshot` | `tab_id` | 截图 |
| `browser_click` | `ref` | 点击元素 |
| `browser_mouse_click_xy` | `x`, `y` | 坐标点击 |
| `browser_fill` | `ref`, `value` | 填写表单 |
| `browser_type` | `ref`, `text` | 输入文本 |
| `browser_hover` | `ref` | 悬停元素 |
| `browser_scroll` | `scroll_x`, `scroll_y`, `scrollIntoView` | 滚动 |
| `browser_console_messages` | `tab_id` | 控制台消息 |
| `browser_network_requests` | `tab_id` | 网络请求 |

## 注意事项

1. **截图优先**：重要差异必须截图记录
2. **版本对比**：始终同时验证 React 和 Angular
3. **交互验证**：不仅看静态样式，还要测试交互
4. **国际化**：确保无硬编码文本
5. **错误检查**：验证控制台无 Error 级别消息
