# RAG 服务

生产级 RAG（检索增强生成）服务，基于 Qdrant 向量数据库。

## 功能特性

- **多格式文档支持**：Markdown、PDF、网页、纯文本
- **向量检索**：Qdrant 驱动的语义搜索
- **灵活的 LLM 支持**：OpenAI GPT、Anthropic Claude、Ollama（本地）
- **流式响应**：实时令牌流输出
- **对话历史**：基于会话的聊天历史记录
- **Docker 部署**：一键部署 Qdrant

## 架构设计

```
┌─────────────────────────────────────────────────────┐
│                    RAG 服务                          │
│                     端口 8001                         │
├─────────────────────────────────────────────────────┤
│  API 层                                              │
│  ├── /documents (上传、列表、删除、统计)             │
│  └── /chat (查询、流式响应、历史记录)                │
├─────────────────────────────────────────────────────┤
│  核心组件                                            │
│  ├── LLM 网关 (OpenAI/Claude/Ollama)               │
│  ├── Embedding 模型 (Sentence Transformers)         │
│  └── 向量存储 (Qdrant)                              │
├─────────────────────────────────────────────────────┤
│  文档处理                                            │
│  ├── 文档加载器 (PDF、MD、网页、文本)               │
│  └── 数据摄取服务 (分块、嵌入)                       │
└─────────────────────────────────────────────────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │  Qdrant 向量数据库   │
         │     端口 6333        │
         └─────────────────────┘
```

## 快速开始

### 1. 克隆并配置

```bash
cd services/rag

# 创建虚拟环境
python -m venv .venv
source .venv/bin/activate

# 安装依赖
pip install -e .
```

### 2. 配置

```bash
cp .env.example .env

# 编辑 .env 文件
vim .env
```

必需的环境变量：

```env
# Qdrant（非 Docker 模式）
QDRANT_HOST=localhost
QDRANT_PORT=6333

# LLM 提供商
LLM_PROVIDER=openai
LLM_MODEL=gpt-4o-mini
OPENAI_API_KEY=your-api-key

# 或使用 Ollama
LLM_PROVIDER=ollama
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=qwen2.5:7b
```

### 3. 启动 Qdrant（使用 Docker）

```bash
docker compose up qdrant -d
```

### 4. 运行服务

```bash
uvicorn src.main:app --host 0.0.0.0 --port 8001 --reload
```

### 5. 验证

```bash
curl http://localhost:8001/health
```

## API 参考

### 文档管理

#### 上传文档

```bash
curl -X POST http://localhost:8001/documents/upload \
  -F "file=@document.pdf" \
  -F "title=My Document"
```

#### 从 URL 摄取

```bash
curl -X POST "http://localhost:8001/documents/ingest-url?url=https://example.com&title=Example"
```

#### 列出文档

```bash
curl http://localhost:8001/documents/
```

#### 删除文档

```bash
curl -X DELETE http://localhost:8001/documents/{doc_id}
```

### 对话

#### 查询

```bash
curl -X POST http://localhost:8001/chat/ \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What is the main topic of the documents?",
    "session_id": "my-session",
    "top_k": 5
  }'
```

#### 流式响应

```bash
curl -X POST http://localhost:8001/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"query": "Explain RAG", "session_id": "stream-test"}'
```

#### 获取对话历史

```bash
curl http://localhost:8001/chat/history/my-session
```

### 直接摄取文本

```bash
curl -X POST "http://localhost:8001/chat/ingest-text?text=Hello%20World&title=Test"
```

## 配置选项

| 变量 | 默认值 | 说明 |
|----------|---------|-------------|
| `HOST` | 0.0.0.0 | 服务主机地址 |
| `PORT` | 8001 | 服务端口 |
| `QDRANT_HOST` | localhost | Qdrant 服务器地址 |
| `QDRANT_PORT` | 6333 | Qdrant 服务器端口 |
| `COLLECTION_NAME` | ai_test_docs | 向量集合名称 |
| `VECTOR_DIM` | 384 | Embedding 维度 |
| `EMBEDDING_MODEL` | all-MiniLM-L6-v2 | Sentence transformer 模型 |
| `EMBEDDING_DEVICE` | cuda | cuda 或 cpu |
| `LLM_PROVIDER` | openai | openai、anthropic 或 ollama |
| `LLM_MODEL` | gpt-4o-mini | 模型名称 |
| `CHUNK_SIZE` | 500 | 文本分块大小（token） |
| `CHUNK_OVERLAP` | 50 | 分块重叠大小 |

## Docker 部署

### 完整部署（RAG + Qdrant）

```bash
docker compose up -d
```

### 仅 RAG（外部 Qdrant）

```bash
docker build -t rag-service .
docker run -p 8001:8001 \
  -e QDRANT_HOST=your-qdrant-host \
  -e OPENAI_API_KEY=your-key \
  rag-service
```

## 开发

### 运行测试

```bash
# 安装开发依赖
pip install -e ".[dev]"

# 运行测试
pytest tests/ -v

# 带覆盖率
pytest tests/ -v --cov=src --cov-report=html
```

### 项目结构

```
services/rag/
├── src/
│   ├── main.py              # FastAPI 应用入口
│   ├── config.py            # 配置管理
│   ├── schemas.py           # Pydantic 模型
│   ├── api/
│   │   ├── documents.py     # 文档 API 路由
│   │   └── chat.py          # 聊天 API 路由
│   ├── core/
│   │   ├── llm_gateway.py   # LLM 提供商抽象
│   │   ├── embedding.py     # Embedding 模型
│   │   └── vector_store.py  # Qdrant 集成
│   ├── document_loader/
│   │   └── loader.py        # 文档加载器
│   └── services/
│       ├── ingestion.py     # 文档摄取
│       └── rag_chain.py     # RAG 链逻辑
├── tests/
│   ├── conftest.py          # 测试 fixtures
│   └── test_rag_api.py      # 单元测试
├── Dockerfile
├── docker-compose.yml
└── pyproject.toml
```

## 支持的文档格式

| 格式 | 扩展名 | 说明 |
|--------|-----------|-------------|
| Markdown | .md, .markdown | 转换为纯文本处理 |
| PDF | .pdf | 文本提取 |
| 网页 | URL | HTML 解析 |
| 纯文本 | .txt | 直接处理 |

## 许可证

MIT
