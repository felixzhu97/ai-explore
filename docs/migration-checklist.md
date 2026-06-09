# Python → Java API 迁移检查清单

> 生成时间: 2026-06-09
> 服务状态: 4 健康 | 1 降级 (Python RAG Qdrant 未连接) | 3 未运行 (Java RAG, Python AI Agents, Python Text)

---

## 服务状态总览

| 服务 | 端口 | 状态 | 说明 |
|------|------|------|------|
| Java Gateway | 9000 | ✅ 健康 | 所有 API 入口 |
| Java RAG Service | 9001 | ❌ 未运行 | - |
| Python AI Agents | 8003 | ❌ 未运行 | - |
| Python Vision | 8000 | ✅ 健康 | - |
| Python TTS | 8013 | ✅ 健康 | - |
| Python Text Service | 8006 | ❌ 未运行 | - |
| Python Media Gen | 8015 | ✅ 健康 | - |
| Python RAG (legacy) | 8001 | ⚠️ 降级 | Qdrant 未连接 |

---

## 1. TTS Service (端口 9000/tts vs 8013/tts)

### ✅ 已对齐端点
| 端点 | 状态 | 说明 |
|------|------|------|
| `GET /tts/health` | ⚠️ 部分对齐 | 字段命名需统一 |
| `GET /tts/providers` | ⚠️ 部分对齐 | 返回 provider 数量不一致 |
| `GET /tts/voices` | ✅ 完全对齐 | - |
| `POST /tts/synthesize` | ✅ 完全对齐 | 二进制音频 |
| `POST /tts/stream` | ✅ 完全对齐 | 流式音频 |

### 🔧 修复项

- [ ] **统一响应字段命名规范**
  - [ ] `providerStatus` → `provider_status` (与 Python 保持一致)
  - [ ] `displayName` → `display_name`
  - [ ] `supportedLanguages` → `supported_languages`

- [ ] **补全 providers 响应字段**
  - [ ] 添加 `is_active` 字段到每个 provider
  - [ ] 返回所有已配置的 providers（Python 返回 5 个：azure, google, elevenlabs, coqui, edge）

- [ ] **补全 health 响应字段**
  - [ ] 添加 `components` 对象：`{config, provider, cache}`

- [ ] **修复断端点**
  - [ ] `GET /tts/provider` (当前 provider) - Java 500 / Python 404
  - [ ] `GET /tts/models` - Java 500 / Python 404

---

## 2. Vision Service (端口 9000/api/vision vs 8000)

### ✅ 关键问题

- [ ] **ML Providers 未初始化**
  - [ ] OCR provider (Tesseract)
  - [ ] YOLO object detection
  - [ ] BLIP captioning
  - [ ] Stable Diffusion image generation
  - [ ] → 导致所有视觉任务返回 `INTERNAL_ERROR`

- [ ] **统一 Content-Type**
  - [ ] Java 当前使用 `multipart/form-data`
  - [ ] Python 使用 JSON body + `imageUrl` 或 base64
  - [ ] 建议：统一为 JSON + base64/URL 方案

- [ ] **统一路径前缀**
  - [ ] Java: `/api/vision/*`
  - [ ] Python: `/vision/*` 或 `/image-gen/*`

- [ ] **统一错误响应格式**
  - [ ] Java: `{"error": "INTERNAL_ERROR"}`
  - [ ] Python: FastAPI 验证错误格式

### 📋 端点差异详情

| 功能 | Java 路径 | Python 路径 | 状态 |
|------|-----------|-------------|------|
| Caption | `/api/vision/caption` | `/vision/caption` | ❌ 均失败 |
| Detect | `/api/vision/detect` | `/vision/detect` | ❌ 均失败 |
| OCR | `/api/vision/ocr` | `/vision/ocr` | ❌ 均失败 |
| Generate | `/api/vision/generate` | `/image-gen/generate` | ⚠️ Java 失败 |
| Variation | `/api/vision/variation` | `/image-gen/variation` | ❌ 不对齐 |
| Upscale | `/api/vision/upscale` | `/image-gen/upscale` | ❌ 不对齐 |
| Video Generate | `/api/vision/video/generate` | 无 | Java 独有 |
| Video Status | `/api/vision/video/status/{id}` | 无 | Java 独有 |

### ➕ Java 独有功能（Python 需补充）
- [ ] `POST /api/vision/video/generate` - 视频生成
- [ ] `POST /api/vision/video/generate/advanced` - 高级视频生成
- [ ] `GET /api/vision/video/status/{taskId}` - 视频任务状态查询

---

## 3. RAG Service (端口 9001/api/rag vs 8001)

> ⚠️ Java RAG Service 未运行，以下基于代码分析

### ✅ 已对齐端点
| 端点 | 状态 | 说明 |
|------|------|------|
| `/api/rag/chat/` | ⚠️ 部分对齐 | 字段命名差异 |
| `/api/rag/chat/stream` | ✅ 完全对齐 | 流式响应 |
| `/api/rag/chat/history/{id}` | ⚠️ 部分对齐 | session_id vs id |
| `/api/rag/documents/` | ✅ 完全对齐 | 列表结构一致 |
| `/api/rag/documents/upload` | ✅ 完全对齐 | 文件上传 |

### 🔧 修复项

- [ ] **统一响应字段命名**
  - [ ] Java: `sessionId` → Python: `session_id`
  - [ ] Java: `docIds` → Python: `doc_ids`
  - [ ] Java: `topK` → Python: `top_k`

- [ ] **补全 chat 响应字段**
  - [ ] 添加 `model` 字段
  - [ ] 添加 `processing_time_ms` 字段

- [ ] **统一路径差异**
  - [ ] Reindex: Java `/documents/{doc_id}/reindex` vs Python `/documents/reindex/{doc_id}`

- [ ] **补全 health 响应**
  - [ ] 添加 `qdrant_connected` 字段
  - [ ] 添加 `embedding_model` 字段
  - [ ] 添加 `llm_provider` 字段

### 📋 端点差异详情

| 功能 | Java 路径 | Python 路径 | 状态 |
|------|-----------|-------------|------|
| Health | `/api/rag/health` | `/health` | ⚠️ 响应不同 |
| Chat | `/api/rag/chat/` | `/api/rag/chat/` | ⚠️ 字段差异 |
| Stream Chat | `/api/rag/chat/stream` | `/api/rag/chat/stream` | ✅ 对齐 |
| Documents | `/api/rag/documents/` | `/api/rag/documents/` | ✅ 对齐 |
| Upload | `/api/rag/documents/upload` | `/api/rag/documents/upload` | ✅ 对齐 |
| Reindex | `/documents/{id}/reindex` | `/documents/reindex/{id}` | ❌ 路径不同 |
| Cache Stats | 无 | `/cache/stats` | ➕ Python 独有 |
| Cache Clear | 无 | `/cache/clear` | ➕ Python 独有 |
| Config Reload | 无 | `/reload` | ➕ Python 独有 |
| Single Doc Get | `/documents/{id}` | 无 | ➕ Java 独有 |
| Detailed Health | `/health/detailed` | 无 | ➕ Java 独有 |

---

## 4. AI Agents Service (端口 9000/api/agents vs 8003)

> ⚠️ Python AI Agents 未运行，以下基于代码分析

### ✅ 部分对齐端点
| 端点 | 状态 | 说明 |
|------|------|------|
| `GET /agents` | ⚠️ 部分对齐 | 字段差异 |
| `POST /supervisor/invoke` | ⚠️ 部分对齐 | 响应格式差异 |
| Streaming | ⚠️ 部分对齐 | Java JSON / Python SSE |

### 🔧 修复项

- [ ] **统一列表响应字段**
  - [ ] Java: 无 `status` 字段 → Python: `{ name, description, status: "online" }`

- [ ] **统一执行请求格式**
  - [ ] Java: `AgentRequestDto { message, agentType, sessionId, topK, model, metadata }`
  - [ ] Python: `InvokeRequest { messages: List[Message], agent_name }`

- [ ] **统一流式响应格式**
  - [ ] Java: JSON 响应
  - [ ] Python: SSE (`event: message\ndata: ...`)

### 📋 端点差异详情

| 功能 | Java 路径 | Python 路径 | 状态 |
|------|-----------|-------------|------|
| List Agents | `/api/agents/agents` | `/agents` | ⚠️ 字段差异 |
| Chat | `/api/agents/chat` | 无 | ➕ Java 独有 |
| Stream Chat | `/api/agents/chat/stream` | 无 | ➕ Java 独有 |
| Supervisor Invoke | `/api/agents/supervisor/invoke` | `/api/agents/supervisor/invoke` | ⚠️ 格式差异 |
| Direct Invoke | `/api/agents/invoke/{type}` | `/api/agents/{name}/invoke` | ⚠️ 格式差异 |
| K8s Domain | `/api/agents/k8s/*` | 无 | ➕ Java 独有 |
| AIOps Domain | `/api/agents/aiops/*` | 无 | ➕ Java 独有 |
| LLMOps Domain | `/api/agents/llmops/*` | 无 | ➕ Java 独有 |
| Workflows | 无 | `/workflows` | ➕ Python 独有 |

---

## 5. Text Service (端口 9000/api/text vs 8006)

> ⚠️ Python Text Service 未运行，以下基于代码分析

### ✅ 已对齐端点
| 端点 | 状态 | 说明 |
|------|------|------|
| Chat | ⚠️ 部分对齐 | 请求格式不同 |
| Completion | ⚠️ 部分对齐 | 请求格式不同 |

### 🔧 修复项

- [ ] **统一请求格式**
  - [ ] Java: 通过 `metadata.operation` 指定操作类型
  - [ ] Python: 使用不同的端点 (`/complete`, `/chat`)

- [ ] **补全 Python 端点**
  - [ ] 添加 `/providers` 端点 - 列出可用 LLM providers
  - [ ] 添加 `/models` 端点 - 列出可用模型
  - [ ] 添加 `/session/{id}` 端点 - 会话管理

### 📋 端点差异详情

| 功能 | Java 路径 | Python 路径 | 状态 |
|------|-----------|-------------|------|
| Chat | `/api/agents/chat` (TEXT agent) | `/api/text/chat` | ⚠️ 架构不同 |
| Complete | `/api/agents/chat` (TEXT agent) | `/api/text/complete` | ⚠️ 架构不同 |
| Translate | `/api/agents/chat` (operation=translate) | 无 | ➕ Java 独有 |
| Summarize | `/api/agents/chat` (operation=summarize) | 无 | ➕ Java 独有 |
| Providers | 无 | `/api/text/providers` | ➕ Python 独有 |
| Models | 无 | `/api/text/models` | ➕ Python 独有 |
| Session | 无 | `/api/text/session/{id}` | ➕ Python 独有 |
| Cache Reset | 无 | `/api/text/reset` | ➕ Python 独有 |

---

## 6. Media Gen Service (端口 9000/image vs 8015)

### ✅ 部分对齐端点
| 端点 | 状态 | 说明 |
|------|------|------|
| `GET /health` | ⚠️ 部分对齐 | 响应字段不同 |
| `POST /image/generate` | ⚠️ 部分对齐 | 字段命名差异 |

### 🔧 修复项

- [ ] **统一响应字段命名**
  - [ ] Java: `negativePrompt` → Python: `negative_prompt`
  - [ ] Java: `cfgScale` → Python: `guidance_scale`
  - [ ] Java: `numInferenceSteps` → Python: `num_inference_steps`
  - [ ] Java: `processingTimeMs` → Python: `processing_time_ms`

- [ ] **统一错误响应格式**
  - [ ] Java: `success`/`error` 字段
  - [ ] Python: HTTP status + detail

### 📋 端点差异详情

| 功能 | Java 路径 | Python 路径 | 状态 |
|------|-----------|-------------|------|
| Health | `/image/health` | `/health` | ⚠️ 字段差异 |
| Generate | `/image/generate` | `/image/generate` | ⚠️ 命名差异 |
| List Models | `/image/models` | 无 | ➕ Java 独有 |
| Get Model | `/image/models/{id}` | 无 | ➕ Java 独有 |
| List LoRAs | `/image/loras` | 无 | ➕ Java 独有 |
| Cache Clear | 无 | `/cache/clear` | ➕ Python 独有 |

---

## 通用修复项

### API 设计规范
- [ ] 统一使用 snake_case 或 camelCase（建议 snake_case 与 Python 一致）
- [ ] 统一错误响应格式：`{error, message, details}`
- [ ] 统一分页格式（如有列表端点）
- [ ] 统一 Content-Type（建议 JSON + base64/URL）

### 测试覆盖
- [ ] 确保 Java 每个端点都有对应测试
- [ ] 测试二进制响应（synthesize/stream）
- [ ] 测试流式响应（stream）
- [ ] 测试错误场景（400/404/500）

### 文档同步
- [ ] 更新 API 文档与实际实现一致
- [ ] 记录 Java 独有端点
- [ ] 记录 Python 独有端点

---

## 优先级排序

### P0 - 必须修复（阻塞功能）
1. Vision ML Providers 初始化（OCR/YOLO/BLIP/SD）
2. Vision `/api/vision/generate` 图像生成功能
3. TTS `/tts/provider` 断端点修复

### P1 - 重要（功能完整）
4. Content-Type 统一（JSON vs multipart）
5. 命名规范统一（snake_case）
6. 路径前缀统一（Vision: `/api/vision` vs `/vision`）
7. Chat 响应字段补全（model, processing_time_ms）

### P2 - 优化（体验提升）
8. 统一错误响应格式
9. Python 视频生成端点补充
10. Python Text Service 补充 providers/models 端点
11. 启动缺失服务（Java RAG, Python AI Agents, Python Text）

---

## 服务独有功能汇总

### Java 独有
| 服务 | 独有功能 |
|------|----------|
| Vision | `/video/generate`, `/video/generate/advanced`, `/video/status/{id}` |
| RAG | `/documents/{id}` 单文档查询, `/health/detailed` |
| Agents | `/api/agents/chat`, `/k8s/*`, `/aiops/*`, `/llmops/*`, `/pipeline/*`, `/monitoring/*`, `/vector/*` |
| Text | translate, summarize 操作 |
| Media | `/models`, `/models/{id}`, `/loras` |

### Python 独有
| 服务 | 独有功能 |
|------|----------|
| RAG | `/cache/stats`, `/cache/clear`, `/reload`, `/documents/database` |
| Agents | `/workflows` |
| Text | `/providers`, `/models`, `/session/{id}`, `/reset` |
| Media | `/cache/clear` |
