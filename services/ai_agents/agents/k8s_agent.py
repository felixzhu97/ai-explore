"""Kubernetes Agent implementation."""

from typing import Any, Dict, List, Optional

from langchain_core.language_models import BaseChatModel
from langchain_core.messages import BaseMessage, HumanMessage
from langchain_core.runnables import Runnable
from langchain_core.tools import BaseTool
from langgraph.graph import StateGraph, END

from services.ai_agents.core.base import BaseInfraAgent, AgentState
from services.ai_agents.core.prompts import K8S_SYSTEM_PROMPT


class K8sAgent(BaseInfraAgent):
    """Agent for Kubernetes cluster management."""
    
    def __init__(
        self,
        llm: BaseChatModel,
        tools: Optional[List[BaseTool]] = None,
        system_prompt: Optional[str] = None,
    ):
        _prompt = system_prompt or K8S_SYSTEM_PROMPT
        super().__init__(
            llm=llm,
            tools=tools or [],
            system_prompt=_prompt,
            name="K8sAgent",
            description="Manages Kubernetes clusters, pods, deployments, and services",
        )
    
    def create_graph(self) -> StateGraph:
        workflow = StateGraph(AgentState)
        workflow.add_node("process", self._create_process_node())
        workflow.set_entry_point("process")
        workflow.add_edge("process", END)
        return workflow.compile()
