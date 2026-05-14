# AI Vision Service - 文档

欢迎使用 AI Vision Service 文档。这是一个全栈 AI 图像分析平台，提供目标检测、图像描述生成、OCR 识别功能，以及生产级 RAG 服务。

## 文档目录

| 文档 | 说明 |
|----------|-------------|
| [快速开始](./QUICKSTART.md) | 5 分钟快速上手 |
| [架构设计](./ARCHITECTURE.md) | 系统设计与组件概览 |
| [API 参考](./API.md) | AI 服务 REST API 端点 |
| [开发指南](./DEVELOPMENT.md) | 本地开发环境配置和工作流程 |
| [RAG 服务](../services/rag/README.md) | RAG 服务文档 |

## 项目概览

AI Vision Service 是一个 TypeScript/Python monorepo 项目，整合了以下组件：

- **React 前端** (`apps/web`) - 用于图像上传和结果展示的用户界面
- **Express.js 服务端** (`apps/server`) - 后端工具接口
- **Python AI 服务** (`services/vision-service`) - 基于 FastAPI 的视觉 AI，集成了 YOLO、BLIP 和 PaddleOCR
- **RAG 服务** (`services/rag`) - 生产级 RAG，基于 Qdrant 向量数据库，支持多种 LLM

## 功能特性

| 功能 | 模型 | 使用场景 |
|---------|-------|----------|
| 目标检测 | YOLO11n | 识别并定位图像中的物体 |
| 图像描述生成 | BLIP | 生成自然语言图像描述 |
| OCR 文字识别 | PaddleOCR | 从图像中提取文字 |
| 综合分析 | 全部模型 | 对单张图像执行所有任务 |
| 文档问答 | RAG | 基于上传的文档回答问题 |
| 语义搜索 | Qdrant | 在文档集合中查找相关内容 |

## 技术栈

### 前端
- React 18
- Vite
- TypeScript

### 后端
- Node.js / Express.js
- Python / FastAPI

### AI 模型
- [Ultralytics YOLO](https://github.com/ultralytics/ultralytics)
- [HuggingFace BLIP](https://huggingface.co/Salesforce/blip-image-captioning-large)
- [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR)

### RAG
- [Qdrant 向量数据库](https://qdrant.tech/)
- [LangChain](https://langchain.com/)
- [Sentence Transformers](https://www.sbert.net/)

## 快速链接

- [GitHub 仓库](https://github.com)
- [API 文档](./API.md)
- [开发指南](./DEVELOPMENT.md)
- [RAG 服务文档](../services/rag/README.md)
