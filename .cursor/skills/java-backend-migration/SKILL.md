---
name: java-backend-migration
description: 迁移后端从 Python/FastAPI 微服务迁移到 Java 单体应用 + 多 Agent 架构。包含 Spring Boot 单体项目结构、DDD 分层、Agent 骨架、Supervisor 编排、RAG/TTS/Vision/Media Agent 实现指南。
---

# Java 单体多 Agent 后端迁移指南

## 目标架构

将 Python 微服务收敛为 **Java 单体应用**，以 DDD + Port & Adapter 风格支持多 Agent 编排，形成统一后端入口。

## 当前状态
- `apps/server-java/gateway`：Java 21 + Spring Boot 3.4 + Gradle 单体主模块
- `apps/server-java/common`：共享 DTO/异常处理
- `services/ai_agents`：Python 侧多 Agent 实现（Supervisor / BaseAgent / LangGraph）
- 其他服务仍以 Python/FastAPI 存在

## 项目结构（单体）

```
apps/server-java/
├── build.gradle.kts
├── settings.gradle.kts
├── common/
│   ├── build.gradle.kts
│   └── src/main/java/com/ai/common/
│       ├── dto/
│       │   ├── ApiResponse.java
│       │   ├── ChatMessage.java
│       │   └── PageRequest.java
│       └── exception/
│           ├── BusinessException.java
│           └── GlobalExceptionHandler.java
├── gateway/
│   ├── build.gradle.kts
│   └── src/main/java/com/ai/
│       ├── GatewayApplication.java
│       ├── config/
│       │   └── WebConfig.java
│       ├── agent/
│       │   ├── domain/
│       │   │   ├── Agent.java
│       │   │   ├── AgentResult.java
│       │   │   ├── Task.java
│       │   │   └── port/
│       │   │       ├── LlmProvider.java
│       │   │       ├── ToolProvider.java
│       │   │       ├── AgentOrchestrator.java
│       │   │       └── ...
│       │   ├── application/
│       │   │   ├── supervisor/
│       │   │   │   ├── SupervisorAgent.java
│       │   │   │   └── SupervisorService.java
│       │   │   ├── rag/
│       │   │   │   ├── RagAgent.java
│       │   │   │   └── RagService.java
│       │   │   ├── text/
│       │   │   │   ├── TextAgent.java
│       │   │   │   └── TextService.java
│       │   │   ├── tts/
│       │   │   │   ├── TtsAgent.java
│       │   │   │   └── TtsService.java
│       │   │   ├── vision/
│       │   │   │   ├── VisionAgent.java
│       │   │   │   └── VisionService.java
│       │   │   └── media/
│       │   │       ├── MediaAgent.java
│       │   │       └── MediaService.java
│       │   └── presentation/
│       │       └── controller/
│       │           ├── AgentController.java
│       │           ├── RagController.java
│       │           └── ...
│       └── infrastructure/
│           └── adapter/
│               ├── langchain4j/
│               │   └── LangChain4jLlmProvider.java
│               └── ...
```

## 依赖配置

```kotlin
// apps/server-java/gateway/build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    implementation("io.projectreactor:reactor-core:3.7.0")
    
    // LangChain4j
    implementation("dev.langchain4j:langchain4j-core:1.5.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.5.0")
    implementation("dev.langchain4j:langchain4j-ollama:1.5.0")
    implementation("dev.langchain4j:langchain4j-qdrant:1.5.0")
    
    // Vector DB
    implementation("io.qdrant:client:1.9.0")
    
    implementation(project(":common"))
}
```

## Part 1: Agent 领域模型

### Agent 接口

```java
// agent/domain/Agent.java
public interface Agent {
    String name();
    String description();
    AgentResult execute(Task task);
}
```

### Task & AgentResult

```java
// agent/domain/Task.java
public record Task(
    String id,
    String type,
    Map<String, Object> input,
    Map<String, Object> context
) {}

// agent/domain/AgentResult.java
public record AgentResult(
    String agentName,
    boolean success,
    Object output,
    List<String> errors
) {}
```

### AgentOrchestrator（Supervisor）

```java
// agent/domain/port/AgentOrchestrator.java
public interface AgentOrchestrator {
    List<String> availableAgents();
    AgentResult route(Task task);
    AgentResult invoke(String agentName, Task task);
}
```

## Part 2: RAG Agent 实现

```java
// agent/application/rag/RagAgent.java
@Service
@RequiredArgsConstructor
public class RagAgent implements Agent {
    
    private final VectorSearchService vectorSearchService;
    private final ChatLanguageModel chatModel;
    
    @Override
    public String name() {
        return "rag";
    }
    
    @Override
    public AgentResult execute(Task task) {
        String query = (String) task.input().get("query");
        List<String> contexts = vectorSearchService.searchSimilar(query, 5);
        
        String prompt = "Context: " + String.join("\n", contexts) 
            + "\n\nQuestion: " + query;
        
        String answer = chatModel.generate(prompt);
        return new AgentResult("rag", true, Map.of("answer", answer), List.of());
    }
}
```

## Part 3: Supervisor Agent 编排

```java
// agent/application/supervisor/SupervisorAgent.java
@Service
@RequiredArgsConstructor
public class SupervisorAgent implements AgentOrchestrator {
    
    private final Map<String, Agent> agents;
    private final IntentClassifier intentClassifier;
    
    @Override
    public AgentResult route(Task task) {
        String userMessage = (String) task.input().get("message");
        String agentName = intentClassifier.classify(userMessage);
        return invoke(agentName, task);
    }
    
    @Override
    public AgentResult invoke(String agentName, Task task) {
        Agent agent = agents.get(agentName);
        if (agent == null) {
            return new AgentResult(agentName, false, null, 
                List.of("Agent not found: " + agentName));
        }
        return agent.execute(task);
    }
}
```

## Part 4: REST API 接入

```java
// agent/presentation/controller/AgentController.java
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {
    
    private final AgentOrchestrator orchestrator;
    
    @PostMapping("/chat")
    public Mono<AgentResult> chat(@RequestBody Task task) {
        return Mono.fromCallable(() -> orchestrator.route(task));
    }
    
    @GetMapping("/list")
    public List<String> listAgents() {
        return orchestrator.availableAgents();
    }
}
```

## Part 5: 迁移策略

### 原则
1. **单体优先**：所有能力收敛到 `gateway` 单应用，端口统一为 `9000`
2. **增量实现**：先骨架，后业务；先 RAG，后 TTS/Vision/Media
3. **保留 Port 抽象**：Domain 层定义接口，Infrastructure 层实现
4. **兼容旧路由**：保留 `/api/rag/**` 等路径，新增 `/api/v1/agents/**`

### Phase 1：骨架 + RAG Agent
- Agent 领域模型
- Supervisor Agent
- RAG Agent（向量检索 + LLM）

### Phase 2：TTS + Vision Agent
- TTS Agent（Edge/Azure/Google/OpenAI）
- Vision Agent（YOLO/BLIP/OCR）

### Phase 3：Media Agent + 低代码编排
- Media Generation Agent（Stable Diffusion）
- 工作流引擎（LangGraph 风格状态机）

### Phase 4：事件 + 持久化
- Agent 执行事件
- 对话历史存储
- 配置化 Agent 注册

## 端口配置

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 9000 | 统一入口 |
| Qdrant | 6333 | 向量数据库 |
| Ollama | 11434 | 本地 LLM |

## API 端点

| Method | Endpoint | 说明 |
|--------|----------|------|
| GET | `/api/v1/agents/list` | 列出可用 Agent |
| POST | `/api/v1/agents/chat` | Supervisor 路由 |
| POST | `/api/v1/agents/{name}/invoke` | 直接调用 Agent |
| GET | `/api/v1/agents/health` | 健康检查 |
