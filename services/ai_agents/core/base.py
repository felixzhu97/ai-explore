"""Base classes for AI Infrastructure Agents.

This module provides the base agent class and state definitions
for all specialized agents in the AI infrastructure suite.
"""

from typing import Any, Dict, List, Optional, TypedDict

from langchain_core.language_models import BaseChatModel
from langchain_core.messages import BaseMessage
from langchain_core.runnables import Runnable
from langchain_core.tools import BaseTool


class AgentState(TypedDict):
    """State schema for all agents."""
    messages: List[BaseMessage]
    context: Dict[str, Any]


class BaseInfraAgent:
    """Base class for all infrastructure agents.
    
    This class provides common functionality for:
    - LLM-based reasoning and tool usage
    - State management with LangGraph
    - Message handling and response formatting
    
    Subclasses should:
    1. Define specialized tools
    2. Implement create_graph() for workflow
    3. Set appropriate system prompts
    4. Override _format_system_message() if needed
    """
    
    def __init__(
        self,
        llm: BaseChatModel,
        tools: Optional[List[BaseTool]] = None,
        system_prompt: str = "",
        name: str = "BaseAgent",
        description: str = "Base infrastructure agent",
    ):
        """Initialize the base agent.
        
        Args:
            llm: Language model for reasoning.
            tools: Optional list of tools.
            system_prompt: System prompt for the agent.
            name: Agent name.
            description: Agent description.
        """
        self.llm = llm
        self.tools = tools or []
        self.system_prompt = system_prompt
        self.name = name
        self.description = description
        self._graph: Optional[Runnable] = None
    
    @property
    def available_tools(self) -> List[str]:
        """Get list of available tool names."""
        return [tool.name for tool in self.tools]
    
    def _format_system_message(self) -> BaseMessage:
        """Format the system message.
        
        Returns:
            System message for the agent.
        """
        from langchain_core.messages import SystemMessage
        return SystemMessage(content=self.system_prompt)
    
    def create_graph(self) -> Runnable:
        """Create the LangGraph workflow.
        
        Returns:
            Compiled runnable graph.
        """
        from langgraph.graph import StateGraph, END
        
        workflow = StateGraph(AgentState)
        workflow.add_node("process", self._create_process_node())
        workflow.set_entry_point("process")
        workflow.add_edge("process", END)
        
        return workflow.compile()
    
    def _create_process_node(self):
        """Create the main processing node."""
        def process_node(state: AgentState) -> Dict[str, Any]:
            """Process the input and generate response."""
            messages = state.get("messages", [])
            if not messages:
                return {"messages": [], "context": {}}
            
            # Run the LLM with tools
            if self.tools:
                response = self.llm.bind_tools(
                    self.tools,
                    tool_choice="auto"
                ).invoke(
                    [self._format_system_message()] + messages
                )
            else:
                response = self.llm.invoke(
                    [self._format_system_message()] + messages
                )
            
            return {
                "messages": [response],
                "context": {"agent": self.name}
            }
        
        return process_node
    
    def get_runnable(self) -> Runnable:
        """Get the compiled, executable runnable.
        
        Returns:
            Compiled Runnable.
        """
        if self._graph is None:
            self._graph = self.create_graph()
        return self._graph
    
    def invoke(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """Invoke the agent with input data.
        
        Args:
            input_data: Dictionary with messages and context.
            
        Returns:
            Agent response.
        """
        runnable = self.get_runnable()
        return runnable.invoke(input_data)
    
    def stream(self, input_data: Dict[str, Any]):
        """Stream the agent response.
        
        Args:
            input_data: Dictionary with messages and context.
            
        Yields:
            Streamed response chunks.
        """
        runnable = self.get_runnable()
        return runnable.stream(input_data)
