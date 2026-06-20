---
title: AI-Test Platform 战略演进地图
---

## Wardley Map

```mermaid
%%{init: {'theme': 'base', 'themeVariables': {'primaryColor': '#4A90D9', 'primaryTextColor': '#fff', 'primaryBorderColor': '#2E5090'}}}%%
flowchart TD
    subgraph ANCHOR["👤 用户锚点 (User Anchor)"]
        U["用户"]
        EU["外部 API 消费者"]
    end

    subgraph GENESIS["🌱 Genesis 阶段 (0-25%)\n差异化竞争"]
        MA["Multi-Agent 编排"]
        MCP["MCP 协议"]
        AIOPS["AIOps 智能分析"]
    end

    subgraph CUSTOM["🔧 Custom Built 阶段 (25-50%)\n内部构建"]
        RAG["RAG 对话"]
        VISION["视觉分析"]
        EMBED["Embedding 生成"]
        VSEARCH["向量检索"]
        DOC["文档处理"]
        SESSION["会话管理"]
    end

    subgraph PRODUCT["📦 Product 阶段 (50-75%)\n优化选择"]
        DS["DeepSeek API"]
        DALL["DALL-E API"]
        YTTS["Edge TTS"]
        YOLO["YOLO 检测"]
        OCR["PaddleOCR"]
        SPRING_AI["Spring AI 2.0"]
        NG["Angular 22"]
        SB["Spring Boot 3.5"]
    end

    subgraph COMMODITY["🏭 Commodity 阶段 (75-100%)\n标准化采购"]
        K8S["Kubernetes"]
        DOCKER["Docker"]
        PG["PostgreSQL 17"]
        RD["Redis"]
        PROM["Prometheus"]
        GHA["GitHub Actions"]
        JAVA["Java 25"]
        NODE["Node.js"]
    end

    %% 用户到能力
    U --> AG["Agent 对话"]
    U --> RAG
    U --> VISION
    U --> IMG["图像生成"]
    U --> TTS["语音合成"]
    U --> AIOPS

    %% 依赖关系
    AG --> MA
    AG --> SESSION
    AG --> DS

    RAG --> VSEARCH
    RAG --> EMBED
    RAG --> DOC

    VISION --> YOLO
    VISION --> OCR
    VISION --> DS

    IMG --> DALL
    TTS --> YTTS

    MA --> MCP
    VSEARCH --> PG
    EMBED --> DS
    DOC --> PG

    SESSION --> RD

    DS --> SPRING_AI
    DALL --> SPRING_AI
    YTTS --> SPRING_AI

    SPRING_AI --> SB
    NG --> NODE
    SB --> JAVA

    K8S --> DOCKER
    PG --> DOCKER
    RD --> DOCKER
    PROM --> K8S
    GHA --> K8S

    %% 演化方向
    MA -.->|"evolve"| VSEARCH
    MCP -.->|"evolve"| SPRING_AI
    EMBED -.->|"evolve"| DS
    VISION -.->|"evolve"| YOLO
```

## 战略决策指引

| 演化阶段 | 组件示例 | 战略决策 | 投入建议 |
|---------|---------|----------|----------|
| **Genesis (0-25%)** | Multi-Agent, MCP-Protocol, AIOps | 差异化竞争 | 研发投入，探索创新 |
| **Custom Built (25-50%)** | RAG-Chat, Vision-Analysis, Embedding | 内部构建 | 积累能力，沉淀资产 |
| **Product (50-75%)** | DeepSeek-API, Spring-AI, Angular | 优化选择 | 评估供应商，避免锁定 |
| **Commodity (75-100%)** | Kubernetes, Docker, PostgreSQL | 标准化采购 | 成本优先，自动化 |

## 坐标系统说明

Wardley Map 使用 **OWM (OnlineWardleyMaps)** 坐标格式 `[visibility, evolution]`：

- **Visibility (Y轴)**: 0.0 = 基础设施, 1.0 = 用户可见
- **Evolution (X轴)**: 0.0 = Genesis, 1.0 = Commodity

## 关键洞察

1. **AIOps 面板**处于 Genesis 阶段，需要持续投入探索
2. **向量检索**正在从 Custom Built 向 Product 演进
3. **LLM 推理**高度依赖外部 API，需要考虑多 Provider 策略
4. **基础设施**已高度标准化，无需重复造轮子
