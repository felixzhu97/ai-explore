# Java 单体多智能体迁移方案（Monolith）

## 目标

将当前分布式服务迁移到 Java 单体架构，保留 DDD/Port & Adapter 风格，
同时支持多 Agent 编排��形成“低代码 + 多 Agent”的统一后端。

## 当前状态

- `apps/server-java/gateway`：Java 21 + Spring Boot 3.4 + Gradle
- `apps/server-java/services/rag-service`：仅原型，不完整
- `services/ai_agents`：Python 侧多 Agent 实现（Supervisor / BaseAgent / LangGraph）
- 其他服务仍以 Python/FastAPI 存在

## 迁移策略

1. 以 `gateway` 作为单体主模块，逐步吸收服务能力
2. 保留 Port/UseCase/Adapter 分层，优先“接口不动、实现替换”
3. 先补齐 Agent 骨架，再实现真实业务逻辑
4. 逐步下线独立服务，端口统一到 `9000`

## 阶段目标

- Phase 1：Agent 骨架 + 路由（当前阶段）
- Phase 2：文本、TTS、Media、Vision Agent 实现
- Phase 3：事件 + 状态机 + 持久化
- Phase 4：低代码编排引擎接入