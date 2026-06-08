---
name: python-microservices-migration
description: 将 Python/FastAPI 微服务能力迁移到 Java 单体应用（Spring Boot 3.4）。包含 FastAPI 到 Spring Boot 的 API 映射、LangChain4j 集成、向量数据库适配、多 Provider 架构、DDD 分层迁移指南。
---

# Python 微服务迁移到 Java 单体

> 当前迁移目标为**单体应用**，所有服务能力收敛到 `apps/server-java/gateway`。

## 迁移范围

| Python 服务 | Java 单体模块 | 状态 |
|-------------|---------------|------|
| `services/ai_agents` | `gateway/src/main/java/com/ai/agent/` | 规划中 |
| `services/rag` | `gateway/.../agent/application/rag/` | 规划中 |
| `services/tts-service` | `gateway/.../agent/application/tts/` | 规划中 |
| `services/vision-service` | `gateway/.../agent/application/vision/` | 规划中 |
| `services/text-service` | `gateway/.../agent/application/text/` | 规划中 |
| `services/media-gen` | `gateway/.../agent/application/media/` | 规划中 |

## 单体项目结构

```
apps/server-java/gateway/src/main/java/com/ai/
├── GatewayApplication.java
├── config/
├── agent/
│   ├── domain/
│   │   ├── Agent.java
│   │   ├── AgentResult.java
│   │   ├── Task.java
│   │   └── port/
│   │       ├── LlmProvider.java
│   │       ├── ToolProvider.java
│   │       └── AgentOrchestrator.java
│   ├── application/
│   │   ├── supervisor/
│   │   │   └── SupervisorAgent.java
│   │   ├── rag/
│   │   │   ├── RagAgent.java
│   │   │   └── RagService.java
│   │   ├── tts/
│   │   │   ├── TtsAgent.java
│   │   │   └── TtsService.java
│   │   ├── text/
│   │   │   ├── TextAgent.java
│   │   │   └── TextService.java
│   │   ├── vision/
│   │   │   ├── VisionAgent.java
│   │   │   └── VisionService.java
│   │   └── media/
│   │       ├── MediaAgent.java
│   │       └── MediaService.java
│   └── presentation/
│       └── controller/
│           ├── AgentController.java
│           └── HealthController.java
└── infrastructure/
    └── adapter/
        ├── langchain4j/
        └── ...
```

## API 映射对照

### FastAPI → Spring Boot WebFlux

| FastAPI | Spring Boot |
|---------|-------------|
| `@router.post("/path")` | `@PostMapping("/path")` |
| `async def endpoint()` | `public Mono<Response> endpoint()` |
| `UploadFile = File(...)` | `@RequestPart MultipartFile file` |
| `Query(param)` | `@RequestParam` |
| `Path(param)` | `@PathVariable` |
| `Body(param)` | `@RequestBody` |
| `HTTPException` | `@ExceptionHandler` / `ResponseStatusException` |

### Python Domain Port → Java Interface

```java
// domain/port/LlmProvider.java
public interface LlmProvider {
    Mono<String> generate(String prompt);
    Flux<String> stream(String prompt);
}
```

```java
// domain/port/ToolProvider.java
public interface ToolProvider {
    String name();
    String description();
    Mono<ToolResult> execute(Map<String, Object> input);
}
```

## 迁移步骤

### Step 1: Agent 骨架
1. 定义 `Agent`、`Task`、`AgentResult` 领域对象
2. 实现 `AgentOrchestrator` 接口
3. 创建 `SupervisorAgent` 路由逻辑

### Step 2: RAG Agent
1. 向量检索接口（LangChain4j + Qdrant）
2. RAG Agent 实现
3. REST 端点接入

### Step 3: 其他 Agent
1. TTS Agent（多 Provider）
2. Vision Agent（YOLO/BLIP）
3. Media Agent（图像生成）

### Step 4: 低代码编排
1. 工作流状态机
2. Agent 链式调用
3. 配置化路由

## 验证方式

### 启动验证
```bash
cd apps/server-java
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.11/libexec/openjdk.jdk/Contents/Home
gradle :gateway:bootRun
```

### 接口验证
```bash
# 健康检查
curl http://localhost:9000/actuator/health

# Agent 列表
curl http://localhost:9000/api/v1/agents/list

# 对话
curl -X POST http://localhost:9000/api/v1/agents/chat \
  -H "Content-Type: application/json" \
  -d '{"type":"chat","input":{"message":"Hello"}}'
```

## 注意事项

1. **单体优先**：所有新功能先放在 `gateway` 模块
2. **保留抽象**：Domain 层只依赖 Port，不依赖具体实现
3. **渐进迁移**：一个 Agent 一个 Agent 实现，每个都可独立验证
4. **兼容性**：保留旧 Python 服务直到 Java 侧完全替代
