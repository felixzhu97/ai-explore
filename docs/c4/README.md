# C4 Model Documentation

本文档使用 C4 模型架构描述方法，详细展示系统的不同抽象层次。

## C4 层次概述


| 层次  | 名称               | 描述                 |
| --- | ---------------- | ------------------ |
| C1  | 系统上下文图 (Context) | 显示整个系统及其用户/外部系统的关系 |
| C2  | 容器图 (Container)  | 显示应用的技术架构和主要容器     |
| C3  | 组件图 (Component)  | 显示每个容器的组件结构        |


## 图表文件

### C1 - 系统上下文图

- **文件**: `context.puml`
- **内容**: 展示 AI-Test Platform 与外部用户、外部系统的交互
- **使用者**: 业务涉众、技术决策者

### C2 - 容器图

- **文件**: `container.puml`
- **内容**: 展示前端、服务层、AI Agents 内部架构、数据层的技术选型
- **使用者**: 架构师、开发团队

### C3 - 组件图


| 文件                           | 描述               | 主要组件                                                                                                                    |
| ---------------------------- | ---------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `component-ai-agents.puml`   | AI Agents 服务内部架构 | Supervisor Agent, RAG Agent, LLMOps Agent, AIOps Agent, Pipeline Agent, Feature Store Agent, LangGraph Workflows, Tools |
| `component-frontend.puml`    | Web 前端组件结构       | App, Panels (Supervisor/K8s/Monitoring/etc), Agent Components                                                           |
| `component-rag-service.puml` | RAG 服务内部架构       | Document API, Chat API, Ingestion Service, RAG Chain, Embedding, Vector Store                                           |


## 查看图表

### 方法 1: PlantUML Online Editor

1. 访问 [PlantUML Online Editor](https://www.plantuml.com/plantuml/uml/)
2. 粘贴 `.puml` 文件内容
3. 点击 "Submit" 渲染图表

### 方法 2: IDE 插件

**VS Code / Cursor:**

- 安装 "PlantUML" 扩展
- 右键 `.puml` 文件选择 "Preview Current Diagram"

**IntelliJ IDEA:**

- 安装 "PlantUML Integration" 插件

### 方法 3: 命令行渲染

```bash
# 安装 PlantUML
brew install plantuml  # macOS
# 或
sudo apt install plantuml  # Ubuntu/Debian

# 渲染 PUML 文件为 PNG
plantuml -o ./output context.puml container.puml
```

## 系统架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用户层                                          │
│    ML 工程师 ●  运维工程师 ●  数据科学家 ●  开发人员                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           AI-Test Platform                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         前端层 (React)                               │   │
│  │  Web App ─┬─ SupervisorPanel ── AgentChat ── ChatMessage         │   │
│  │            ├─ K8sPanel                                             │   │
│  │            ├─ MonitoringPanel                                      │   │
│  │            ├─ ModelPanel / LLMOpsPanel                             │   │
│  │            ├─ AIOpsPanel                                           │   │
│  │            └─ VectorDBPanel                                        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       AI Agents 服务层 (LangGraph)                    │   │
│  │                                                                       │   │
│  │  ┌─────────────────┐                                                │   │
│  │  │ SupervisorAgent │ ◄── 主管智能体 (路由协调)                      │   │
│  │  └────────┬────────┘                                                │   │
│  │           │                                                         │   │
│  │  ┌────────┼────────┬──────────┬──────────┐                         │   │
│  │  ▼        ▼        ▼          ▼          ▼                         │   │
│  │ ┌─────┐ ┌───────┐ ┌────────┐ ┌────────┐ ┌──────────┐              │   │
│  │ │RAG  │ │LLMOps │ │ AIOps  │ │Pipeline│ │Feature   │              │   │
│  │ │Agent│ │Agent  │ │ Agent  │ │ Agent  │ │StoreAgent│              │   │
│  │ └──┬──┘ └──┬───┘ └───┬────┘ └───┬────┘ └───┬───────┘              │   │
│  │    │        │         │          │          │                      │   │
│  │    └────────┴─────────┴──────────┴──────────┴─── LangGraph ────► Tools
│  │                                                      Workflows        │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│          ┌─────────────────────────┼─────────────────────────┐              │
│          ▼                         ▼                         ▼              │
│  ┌───────────────┐      ┌─────────────────┐      ┌─────────────────┐       │
│  │Vision Service │      │  RAG Service   │      │ Backend Server  │       │
│  │ (YOLO/BLIP)   │      │ (LangChain/Qdr)│      │   (Express)     │       │
│  └───────────────┘      └────────┬────────┘      └─────────────────┘       │
│                                   │                                         │
│                                   ▼                                         │
│                          ┌───────────────┐                                  │
│                          │ Vector Store  │                                  │
│                          │   (Qdrant)    │                                  │
│                          └───────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          外部集成服务                                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ OpenAI   │  │ Claude   │  │ Ollama   │  │ MLflow   │  │ Feast    │    │
│  │  API     │  │   API    │  │ (Local)  │  │          │  │          │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│                                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                                  │
│  │  K8s     │  │Prometheus│  │ Grafana  │                                  │
│  │          │  │          │  │          │                                  │
│  └──────────┘  └──────────┘  └──────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 智能体路由配置

Supervisor Agent 根据用户输入的关键词路由到相应的专业智能体：

```
用户输入 → Supervisor Agent → 关键词匹配 → 专业智能体

路由关键词映射:
├── "vector", "collection", "search"     → RAG Agent
├── "model", "train", "deploy"          → LLMOps Agent
├── "monitor", "alert", "metric"        → AIOps Agent
├── "pipeline", "workflow", "step"       → Pipeline Agent
├── "feature", "entity"                 → Feature Store Agent
├── "kubernetes", "k8s", "cluster"      → K8s 操作 (AIOps Agent)
└── "incident", "anomaly", "root cause" → AIOps Agent
```

## 技术栈汇总


| 层级            | 技术                               | 用途     |
| ------------- | -------------------------------- | ------ |
| **前端**        | React 18, TypeScript, Vite       | UI 框架  |
| **AI Agents** | Python, LangChain, LangGraph     | 智能体编排  |
| **RAG**       | FastAPI, LangChain, Qdrant       | 检索增强生成 |
| **Vision**    | FastAPI, PyTorch, YOLO, BLIP     | 视觉 AI  |
| **LLM**       | OpenAI, Anthropic Claude, Ollama | 语言模型   |
| **MLOps**     | MLflow                           | 实验跟踪   |
| **Features**  | Feast                            | 特征存储   |
| **Infra**     | Kubernetes, Prometheus, Grafana  | 基础设施   |


