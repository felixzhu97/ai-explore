"""AI Agents Service - FastAPI Application.

This service provides REST API endpoints for multi-agent orchestration.
"""

from contextlib import asynccontextmanager
from typing import Any, Dict, List, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from loguru import logger
import json
import asyncio

from services.ai_agents.agents.supervisor import SupervisorAgent
from services.ai_agents.agents.rag_agent import RAGAgent
from services.ai_agents.agents.llmops_agent import LLMOpsAgent
from services.ai_agents.agents.aiops_agent import AIOpsAgent
from services.ai_agents.agents.pipeline_agent import PipelineAgent
from services.ai_agents.agents.feature_store_agent import FeatureStoreAgent
from services.ai_agents.agents.k8s_agent import K8sAgent
from services.ai_agents.agents.monitoring_agent import MonitoringAgent
from services.ai_agents.agents.vector_db_agent import VectorDBAgent
from services.ai_agents.agents.model_agent import ModelAgent


class Message(BaseModel):
    role: str
    content: str


class InvokeRequest(BaseModel):
    messages: List[Message] = Field(default_factory=list)
    agent_name: Optional[str] = None


class AgentInfo(BaseModel):
    name: str
    description: str
    status: str = "online"


_supervisor: Optional[SupervisorAgent] = None
_initialized = False


async def initialize_agents():
    """Initialize all agents with LLM configuration."""
    global _supervisor, _initialized
    
    if _initialized:
        return
    
    try:
        from langchain_ollama import ChatOllama
        from services.ai_agents.core.config import get_settings
        
        settings = get_settings()
        
        # Create LLM instance using Ollama
        llm = ChatOllama(
            model=settings.OLLAMA_MODEL,
            base_url=settings.OLLAMA_BASE_URL,
            temperature=0.7,
        )
        
        logger.info(f"Initializing AI Agents with Ollama ({settings.OLLAMA_BASE_URL}/{settings.OLLAMA_MODEL})...")
        
        # Initialize all specialized agents
        rag_agent = RAGAgent(llm=llm)
        llmops_agent = LLMOpsAgent(llm=llm)
        aiops_agent = AIOpsAgent(llm=llm)
        pipeline_agent = PipelineAgent(llm=llm)
        feature_store_agent = FeatureStoreAgent(llm=llm)
        k8s_agent = K8sAgent(llm=llm)
        monitoring_agent = MonitoringAgent(llm=llm)
        vector_agent = VectorDBAgent(llm=llm)
        model_agent = ModelAgent(llm=llm)
        
        # Create supervisor with all agents
        _supervisor = SupervisorAgent(
            llm=llm,
            rag_agent=rag_agent,
            llmops_agent=llmops_agent,
            aiops_agent=aiops_agent,
            pipeline_agent=pipeline_agent,
            feature_store_agent=feature_store_agent,
            k8s_agent=k8s_agent,
            monitoring_agent=monitoring_agent,
            vector_agent=vector_agent,
            model_agent=model_agent,
        )
        
        logger.info(f"Supervisor initialized with agents: {_supervisor.available_agents}")
        _initialized = True
        
    except Exception as e:
        logger.error(f"Failed to initialize agents: {e}")
        # Create a minimal supervisor without agents for now
        _supervisor = None
        _initialized = True


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting AI Agents Service...")
    await initialize_agents()
    logger.info("AI Agents Service started")
    yield
    logger.info("Shutting down AI Agents Service...")


def create_app() -> FastAPI:
    app = FastAPI(
        title="AI Agents Service",
        description="Multi-agent orchestration service for AI infrastructure",
        version="0.1.0",
        lifespan=lifespan,
    )
    
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    @app.get("/health")
    async def health():
        return {
            "status": "ok",
            "service": "ai_agents",
            "agents_initialized": _supervisor is not None,
            "available_agents": _supervisor.available_agents if _supervisor else [],
        }
    
    @app.get("/agents")
    async def list_agents():
        """List all available agents."""
        if _supervisor is None:
            raise HTTPException(status_code=503, detail="Agents not initialized")
        
        return {
            "agents": [
                {
                    "name": name,
                    "description": desc,
                    "status": "online"
                }
                for name, desc in _supervisor.agent_descriptions.items()
            ]
        }
    
    @app.post("/api/agents/supervisor/invoke")
    async def invoke_supervisor(request: InvokeRequest):
        """Invoke the supervisor agent with a streaming response."""
        if _supervisor is None:
            # Return a mock response if supervisor is not initialized
            async def mock_stream():
                user_message = request.messages[-1].content if request.messages else ""
                
                yield f"event: message\n"
                yield f"data: I understand you're asking about: {user_message[:50]}...\n\n"
                
                yield f"event: message\n"
                yield f"data: However, the AI Agents service is running in demo mode. "
                yield f"data: The supervisor agent would route this request to the appropriate specialized agent.\n\n"
                
                yield f"event: message\n"
                yield f"data: To enable full functionality, please configure your LLM provider (OpenAI API key) in the .env file.\n\n"
                
                yield f"data: [DONE]\n\n"
            
            return StreamingResponse(
                mock_stream(),
                media_type="text/event-stream",
                headers={
                    "Cache-Control": "no-cache",
                    "Connection": "keep-alive",
                },
            )
        
        async def generate():
            try:
                from langchain_core.messages import HumanMessage, AIMessage
                
                # Convert messages to LangChain format
                lc_messages = []
                for msg in request.messages:
                    if msg.role == "user":
                        lc_messages.append(HumanMessage(content=msg.content))
                    elif msg.role == "assistant":
                        lc_messages.append(AIMessage(content=msg.content))
                
                if not lc_messages:
                    yield f"data: No messages provided\n\n"
                    yield f"data: [DONE]\n\n"
                    return
                
                # Invoke supervisor
                result = _supervisor.invoke({"messages": lc_messages})
                
                # Stream the response
                result_messages = result.get("messages", [])
                agent_results = result.get("agent_results", {})
                
                if result_messages:
                    last_message = result_messages[-1]
                    content = last_message.content if hasattr(last_message, 'content') else str(last_message)
                    
                    # Stream in chunks
                    chunk_size = 20
                    for i in range(0, len(content), chunk_size):
                        yield f"event: message\n"
                        yield f"data: {content[i:i+chunk_size]}\n\n"
                        await asyncio.sleep(0.02)
                
                # Send agent results
                for agent_name, agent_result in agent_results.items():
                    yield f"event: tool_output\n"
                    yield f"data: Agent '{agent_name}' completed\n\n"
                
                yield f"data: [DONE]\n\n"
                
            except Exception as e:
                logger.error(f"Error invoking supervisor: {e}")
                yield f"event: error\n"
                yield f"data: Error: {str(e)}\n\n"
        
        return StreamingResponse(
            generate(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
            },
        )
    
    @app.post("/api/agents/{agent_name}/invoke")
    async def invoke_agent(agent_name: str, request: InvokeRequest):
        """Invoke a specific agent directly."""
        if _supervisor is None:
            raise HTTPException(status_code=503, detail="Agents not initialized")
        
        if agent_name not in _supervisor.available_agents:
            raise HTTPException(status_code=404, detail=f"Agent '{agent_name}' not found")
        
        async def generate():
            try:
                from langchain_core.messages import HumanMessage
                
                task = request.messages[-1].content if request.messages else ""
                
                result = _supervisor.invoke_single_agent(agent_name, task)
                
                result_content = result.get("result", str(result))
                if isinstance(result_content, dict):
                    result_content = json.dumps(result_content, indent=2)
                
                yield f"event: message\n"
                yield f"data: {result_content}\n\n"
                yield f"data: [DONE]\n\n"
                
            except Exception as e:
                logger.error(f"Error invoking agent {agent_name}: {e}")
                yield f"event: error\n"
                yield f"data: Error: {str(e)}\n\n"
        
        return StreamingResponse(
            generate(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
            },
        )
    
    return app


app = create_app()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8003,
        reload=True,
    )
