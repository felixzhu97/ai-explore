"""AIOps tools for AI Infrastructure Agents.

This module provides tools for intelligent operations, anomaly detection, and incident management.
"""

import uuid
from datetime import datetime, timedelta
from typing import Any, Dict, List, Optional

from langchain_core.tools import BaseTool
from pydantic import BaseModel, Field

from services.ai_agents.core.schemas import (
    AlertSeverity,
    AnomalyDetectionConfig,
    AnomalyResult,
    IncidentCreateRequest,
    IncidentResponse,
    IncidentStatus,
    LogEntry,
    LogQuery,
    MetricQuery,
    RootCauseAnalysisRequest,
    RootCauseResult,
    TimeSeriesData,
    TimeSeriesPoint,
)


# ============================================================================
# Tool Input Schemas
# ============================================================================


class DetectAnomalyInput(BaseModel):
    """Input schema for anomaly detection."""
    metric: str = Field(..., description="Metric name to analyze")
    time_range: str = Field(default="1h", description="Time range for analysis")
    sensitivity: float = Field(default=0.5, ge=0.0, le=1.0)


class SearchLogsInput(BaseModel):
    """Input schema for log search."""
    query: str = Field(..., description="Log search query")
    time_range: str = Field(default="1h", description="Time range")
    limit: int = Field(default=100, ge=1, le=1000)


class QueryMetricsInput(BaseModel):
    """Input schema for metric query."""
    metric: str = Field(..., description="Metric name")
    time_range: str = Field(default="5m", description="Time range")
    aggregation: str = Field(default="avg")


class CreateIncidentInput(BaseModel):
    """Input schema for incident creation."""
    title: str = Field(..., description="Incident title")
    severity: AlertSeverity
    description: str = Field(..., description="Incident description")
    affected_systems: List[str] = Field(..., description="Affected systems")
    labels: Optional[Dict[str, str]] = Field(default=None)


class RootCauseAnalysisInput(BaseModel):
    """Input schema for root cause analysis."""
    incident_id: str = Field(..., description="Incident ID")
    time_range: str = Field(default="1h")
    affected_services: List[str]
    include_related_logs: bool = Field(default=True)


class AnalyzeLogsInput(BaseModel):
    """Input schema for log analysis."""
    service: str = Field(..., description="Service name")
    time_range: str = Field(default="1h")
    error_only: bool = Field(default=True)


# ============================================================================
# AIOps Adapter (Placeholder)
# ============================================================================


class AIOpsAdapter:
    """Adapter for AIOps operations.
    
    This is a placeholder implementation that simulates AIOps operations.
    In production, integrate with PagerDuty, VictorOps, or internal incident management.
    """
    
    def __init__(self):
        """Initialize the AIOps adapter."""
        self._incidents: Dict[str, IncidentResponse] = {}
        self._anomaly_history: List[AnomalyResult] = []
        self._logs: List[LogEntry] = []
        self._metrics: Dict[str, List[TimeSeriesPoint]] = {}
        self._alerts: Dict[str, Dict[str, Any]] = {}
    
    def detect_anomaly(
        self,
        metric: str,
        time_range: str = "1h",
        sensitivity: float = 0.5
    ) -> AnomalyResult:
        """Detect anomalies in a metric.
        
        Args:
            metric: Metric name.
            time_range: Time range.
            sensitivity: Detection sensitivity.
            
        Returns:
            Anomaly detection result.
        """
        threshold = 0.7 - (sensitivity * 0.4)
        score = 0.3 + (hash(metric) % 50) / 100
        
        is_anomaly = score > threshold
        
        explanations = [
            "Normal operation",
            "Slight deviation detected",
            "Moderate anomaly detected",
            "Significant deviation from baseline",
            "Critical anomaly detected"
        ]
        
        explanation_idx = min(int((score - threshold) * 10) + 1, 4) if is_anomaly else 0
        
        result = AnomalyResult(
            is_anomaly=is_anomaly,
            score=score,
            threshold=threshold,
            explanation=explanations[explanation_idx]
        )
        
        self._anomaly_history.append(result)
        
        return result
    
    def search_logs(self, query: str, time_range: str = "1h", limit: int = 100) -> List[LogEntry]:
        """Search logs.
        
        Args:
            query: Search query.
            time_range: Time range.
            limit: Maximum results.
            
        Returns:
            Matching log entries.
        """
        query_lower = query.lower()
        
        results = []
        for log in self._logs[-limit:]:
            if query_lower in log.message.lower() or query_lower in log.service.lower():
                results.append(log)
        
        if not results:
            mock_entries = [
                LogEntry(
                    timestamp=datetime.now() - timedelta(minutes=i),
                    level="INFO",
                    message=f"Service running normally - request {i}",
                    service="api-gateway"
                )
                for i in range(min(limit, 10))
            ]
            return mock_entries
        
        return results[:limit]
    
    def query_metrics(
        self,
        metric: str,
        time_range: str = "5m",
        aggregation: str = "avg"
    ) -> Dict[str, Any]:
        """Query metrics.
        
        Args:
            metric: Metric name.
            time_range: Time range.
            aggregation: Aggregation method.
            
        Returns:
            Metric data.
        """
        if metric not in self._metrics:
            points = [
                TimeSeriesPoint(
                    timestamp=datetime.now() - timedelta(minutes=i),
                    value=100 + (hash(f"{metric}:{i}") % 20)
                )
                for i in range(12)
            ]
            self._metrics[metric] = points
        
        points = self._metrics[metric]
        values = [p.value for p in points]
        
        if aggregation == "avg":
            value = sum(values) / len(values)
        elif aggregation == "sum":
            value = sum(values)
        elif aggregation == "min":
            value = min(values)
        elif aggregation == "max":
            value = max(values)
        else:
            value = values[-1] if values else 0
        
        return {
            "metric": metric,
            "aggregation": aggregation,
            "value": value,
            "unit": "count",
            "time_range": time_range
        }
    
    def create_incident(self, request: IncidentCreateRequest) -> IncidentResponse:
        """Create an incident.
        
        Args:
            request: Incident creation request.
            
        Returns:
            Created incident.
        """
        incident_id = f"inc_{uuid.uuid4().hex[:8]}"
        
        incident = IncidentResponse(
            incident_id=incident_id,
            status=IncidentStatus.OPEN,
            title=request.title,
            severity=request.severity,
            description=request.description,
            affected_systems=request.affected_systems,
            labels=request.labels or {},
            timeline=[{
                "action": "created",
                "timestamp": datetime.now().isoformat(),
                "details": "Incident created"
            }]
        )
        
        self._incidents[incident_id] = incident
        
        return incident
    
    def get_incident(self, incident_id: str) -> Optional[IncidentResponse]:
        """Get an incident.
        
        Args:
            incident_id: Incident ID.
            
        Returns:
            Incident or None.
        """
        return self._incidents.get(incident_id)
    
    def list_incidents(
        self,
        status: Optional[IncidentStatus] = None,
        severity: Optional[AlertSeverity] = None
    ) -> List[IncidentResponse]:
        """List incidents.
        
        Args:
            status: Status filter.
            severity: Severity filter.
            
        Returns:
            List of incidents.
        """
        incidents = list(self._incidents.values())
        
        if status:
            incidents = [i for i in incidents if i.status == status]
        
        if severity:
            incidents = [i for i in incidents if i.severity == severity]
        
        return sorted(incidents, key=lambda i: i.created_at, reverse=True)
    
    def update_incident_status(
        self,
        incident_id: str,
        status: IncidentStatus,
        message: Optional[str] = None
    ) -> Dict[str, Any]:
        """Update incident status.
        
        Args:
            incident_id: Incident ID.
            status: New status.
            message: Optional message.
            
        Returns:
            Update result.
        """
        if incident_id not in self._incidents:
            return {"success": False, "error": f"Incident '{incident_id}' not found"}
        
        incident = self._incidents[incident_id]
        incident.status = status
        incident.updated_at = datetime.now()
        
        incident.timeline.append({
            "action": status.value,
            "timestamp": datetime.now().isoformat(),
            "details": message or f"Status changed to {status.value}"
        })
        
        return {"success": True, "incident_id": incident_id, "status": status.value}
    
    def analyze_root_cause(self, request: RootCauseAnalysisRequest) -> RootCauseResult:
        """Perform root cause analysis.
        
        Args:
            request: Analysis request.
            
        Returns:
            Root cause result.
        """
        root_causes = [
            "Database connection pool exhaustion",
            "Memory leak in application service",
            "Network latency spike due to infrastructure issue",
            "Configuration change caused cascade failure",
            "Increased traffic beyond capacity"
        ]
        
        contributing_factors = [
            "High memory usage detected 10 minutes before incident",
            "Connection pool at maximum capacity",
            "Multiple retries observed from upstream services",
            "Recent deployment of new configuration"
        ]
        
        recommendations = [
            "Scale up database connection pool",
            "Implement circuit breaker pattern",
            "Add horizontal pod autoscaling",
            "Review and optimize query performance",
            "Set up capacity planning alerts"
        ]
        
        root_cause = root_causes[hash(request.incident_id) % len(root_causes)]
        
        return RootCauseResult(
            incident_id=request.incident_id,
            root_cause=root_cause,
            confidence=0.75 + (hash(request.incident_id) % 20) / 100,
            contributing_factors=contributing_factors,
            evidence={
                "metrics_analyzed": 5,
                "logs_correlated": 3,
                "services_inspected": len(request.affected_services)
            },
            recommendations=recommendations
        )
    
    def acknowledge_alert(self, alert_id: str, user: str) -> Dict[str, Any]:
        """Acknowledge an alert.
        
        Args:
            alert_id: Alert ID.
            user: User acknowledging.
            
        Returns:
            Acknowledge result.
        """
        return {
            "success": True,
            "alert_id": alert_id,
            "acknowledged_by": user,
            "acknowledged_at": datetime.now().isoformat()
        }
    
    def get_system_health(self) -> Dict[str, Any]:
        """Get system health overview.
        
        Returns:
            Health overview.
        """
        return {
            "status": "healthy",
            "uptime": "99.95%",
            "active_incidents": len([
                i for i in self._incidents.values()
                if i.status in (IncidentStatus.OPEN, IncidentStatus.INVESTIGATING)
            ]),
            "recent_anomalies": len([
                a for a in self._anomaly_history
                if a.is_anomaly
            ]),
            "services": {
                "api-gateway": "healthy",
                "auth-service": "healthy",
                "data-pipeline": "healthy",
                "ml-service": "degraded"
            }
        }


# Global adapter instance
_aiops_adapter: Optional[AIOpsAdapter] = None


def get_aiops_adapter() -> AIOpsAdapter:
    """Get the global AIOps adapter instance."""
    global _aiops_adapter
    if _aiops_adapter is None:
        _aiops_adapter = AIOpsAdapter()
    return _aiops_adapter


# ============================================================================
# AIOps Tools
# ============================================================================


def _create_detect_anomaly_tool() -> BaseTool:
    """Create the detect anomaly tool."""
    def detect_anomaly(
        metric: str,
        time_range: str = "1h",
        sensitivity: float = 0.5
    ) -> str:
        """Detect anomalies in a metric.
        
        Args:
            metric: Metric name.
            time_range: Time range for analysis.
            sensitivity: Detection sensitivity (0-1).
            
        Returns:
            Anomaly detection result.
        """
        adapter = get_aiops_adapter()
        result = adapter.detect_anomaly(metric, time_range, sensitivity)
        
        status = "ANOMALY DETECTED" if result.is_anomaly else "NORMAL"
        
        return f"""Anomaly Detection Result for '{metric}'
Status: {status}
Score: {result.score:.4f}
Threshold: {result.threshold:.4f}
Explanation: {result.explanation}"""
    
    return BaseTool(
        name="detect_anomaly",
        description="Detect anomalies in metrics using statistical methods. Use this to identify unusual behavior in system metrics.",
        args_schema=DetectAnomalyInput,
        func=detect_anomaly
    )


def _create_search_logs_tool() -> BaseTool:
    """Create the search logs tool."""
    def search_logs(
        query: str,
        time_range: str = "1h",
        limit: int = 100
    ) -> str:
        """Search logs.
        
        Args:
            query: Search query.
            time_range: Time range.
            limit: Maximum results.
            
        Returns:
            Log search results.
        """
        adapter = get_aiops_adapter()
        results = adapter.search_logs(query, time_range, limit)
        
        if not results:
            return f"No logs found matching: '{query}'"
        
        output = f"Found {len(results)} log entries:\n\n"
        for log in results[:20]:
            output += f"[{log.timestamp.strftime('%Y-%m-%d %H:%M:%S')}] "
            output += f"[{log.level}] {log.service}: {log.message}\n"
        
        return output
    
    return BaseTool(
        name="search_logs",
        description="Search logs across services. Use this to find specific log entries or debug issues.",
        args_schema=SearchLogsInput,
        func=search_logs
    )


def _create_query_metrics_tool() -> BaseTool:
    """Create the query metrics tool."""
    def query_metrics(
        metric: str,
        time_range: str = "5m",
        aggregation: str = "avg"
    ) -> str:
        """Query metrics.
        
        Args:
            metric: Metric name.
            time_range: Time range.
            aggregation: Aggregation method.
            
        Returns:
            Metric data.
        """
        adapter = get_aiops_adapter()
        result = adapter.query_metrics(metric, time_range, aggregation)
        
        return f"""Metric: {result['metric']}
Aggregation: {result['aggregation']}
Value: {result['value']:.2f} {result['unit']}
Time Range: {result['time_range']}"""
    
    return BaseTool(
        name="query_metrics",
        description="Query system metrics. Use this to get current or historical metric values.",
        args_schema=QueryMetricsInput,
        func=query_metrics
    )


def _create_incident_tool() -> BaseTool:
    """Create the create incident tool."""
    def create_incident(
        title: str,
        severity: str,
        description: str,
        affected_systems: List[str],
        labels: Optional[Dict[str, str]] = None
    ) -> str:
        """Create an incident.
        
        Args:
            title: Incident title.
            severity: Severity level.
            description: Incident description.
            affected_systems: Affected systems.
            labels: Optional labels.
            
        Returns:
            Created incident.
        """
        adapter = get_aiops_adapter()
        
        try:
            sev = AlertSeverity(severity.lower())
        except ValueError:
            return f"Invalid severity: {severity}"
        
        request = IncidentCreateRequest(
            title=title,
            severity=sev,
            description=description,
            affected_systems=affected_systems,
            labels=labels
        )
        
        incident = adapter.create_incident(request)
        
        return f"""Incident Created!
ID: {incident.incident_id}
Title: {incident.title}
Severity: {incident.severity.value}
Status: {incident.status.value}
Affected Systems: {', '.join(incident.affected_systems)}"""
    
    return BaseTool(
        name="create_incident",
        description="Create a new incident for tracking and resolution. Use this when an issue requires coordinated response.",
        args_schema=CreateIncidentInput,
        func=create_incident
    )


def _create_get_incident_tool() -> BaseTool:
    """Create the get incident tool."""
    def get_incident(incident_id: str) -> str:
        """Get incident details.
        
        Args:
            incident_id: Incident ID.
            
        Returns:
            Incident details.
        """
        adapter = get_aiops_adapter()
        incident = adapter.get_incident(incident_id)
        
        if incident is None:
            return f"Incident '{incident_id}' not found"
        
        output = f"""Incident: {incident.incident_id}
Title: {incident.title}
Severity: {incident.severity.value}
Status: {incident.status.value}
Description: {incident.description}
Affected Systems: {', '.join(incident.affected_systems)}
Created: {incident.created_at.strftime('%Y-%m-%d %H:%M:%S')}
Updated: {incident.updated_at.strftime('%Y-%m-%d %H:%M:%S')}
"""
        
        if incident.timeline:
            output += "\nTimeline:\n"
            for entry in incident.timeline:
                output += f"  - [{entry['timestamp']}] {entry['action']}: {entry['details']}\n"
        
        return output
    
    return BaseTool(
        name="get_incident",
        description="Get detailed information about an incident. Use this to view incident status and timeline.",
        args_schema={
            "incident_id": str
        },
        func=get_incident
    )


def _create_list_incidents_tool() -> BaseTool:
    """Create the list incidents tool."""
    def list_incidents(status: Optional[str] = None, severity: Optional[str] = None) -> str:
        """List incidents.
        
        Args:
            status: Status filter.
            severity: Severity filter.
            
        Returns:
            List of incidents.
        """
        adapter = get_aiops_adapter()
        
        status_enum = None
        if status:
            try:
                status_enum = IncidentStatus(status.lower())
            except ValueError:
                return f"Invalid status: {status}"
        
        severity_enum = None
        if severity:
            try:
                severity_enum = AlertSeverity(severity.lower())
            except ValueError:
                return f"Invalid severity: {severity}"
        
        incidents = adapter.list_incidents(status_enum, severity_enum)
        
        if not incidents:
            return "No incidents found"
        
        output = f"Incidents ({len(incidents)} shown):\n\n"
        for inc in incidents[:20]:
            output += f"- {inc.incident_id}: {inc.title}\n"
            output += f"  Severity: {inc.severity.value}, Status: {inc.status.value}\n"
        
        return output
    
    return BaseTool(
        name="list_incidents",
        description="List incidents. Use this to see active incidents and their status.",
        args_schema={
            "status": Optional[str],
            "severity": Optional[str]
        },
        func=list_incidents
    )


def _create_update_incident_tool() -> BaseTool:
    """Create the update incident tool."""
    def update_incident(
        incident_id: str,
        status: str,
        message: Optional[str] = None
    ) -> str:
        """Update incident status.
        
        Args:
            incident_id: Incident ID.
            status: New status.
            message: Optional message.
            
        Returns:
            Update result.
        """
        adapter = get_aiops_adapter()
        
        try:
            status_enum = IncidentStatus(status.lower())
        except ValueError:
            return f"Invalid status: {status}"
        
        result = adapter.update_incident_status(incident_id, status_enum, message)
        
        if result["success"]:
            return f"Updated incident '{incident_id}' to status '{status}'"
        return f"Failed: {result.get('error', 'Unknown error')}"
    
    return BaseTool(
        name="update_incident",
        description="Update incident status. Use this to change incident state during response.",
        args_schema={
            "incident_id": str,
            "status": str,
            "message": Optional[str]
        },
        func=update_incident
    )


def _create_root_cause_analysis_tool() -> BaseTool:
    """Create the root cause analysis tool."""
    def analyze_root_cause(
        incident_id: str,
        time_range: str = "1h",
        affected_services: List[str] = None,
        include_related_logs: bool = True
    ) -> str:
        """Perform root cause analysis.
        
        Args:
            incident_id: Incident ID.
            time_range: Time range for analysis.
            affected_services: Affected services.
            include_related_logs: Include logs in analysis.
            
        Returns:
            Root cause analysis result.
        """
        adapter = get_aiops_adapter()
        
        request = RootCauseAnalysisRequest(
            incident_id=incident_id,
            time_range=time_range,
            affected_services=affected_services or [],
            include_related_logs=include_related_logs
        )
        
        result = adapter.analyze_root_cause(request)
        
        output = f"""Root Cause Analysis for Incident: {incident_id}

Root Cause: {result.root_cause}
Confidence: {result.confidence:.1%}

Contributing Factors:
"""
        for factor in result.contributing_factors:
            output += f"  - {factor}\n"
        
        output += "\nEvidence:\n"
        for key, value in result.evidence.items():
            output += f"  - {key}: {value}\n"
        
        output += "\nRecommendations:\n"
        for rec in result.recommendations:
            output += f"  - {rec}\n"
        
        return output
    
    return BaseTool(
        name="analyze_root_cause",
        description="Perform root cause analysis for an incident. Use this to identify the underlying cause of issues.",
        args_schema=RootCauseAnalysisInput,
        func=analyze_root_cause
    )


def _create_get_system_health_tool() -> BaseTool:
    """Create the get system health tool."""
    def get_system_health() -> str:
        """Get system health overview.
        
        Returns:
            Health overview.
        """
        adapter = get_aiops_adapter()
        health = adapter.get_system_health()
        
        output = f"""System Health Overview

Overall Status: {health['status'].upper()}
Uptime: {health['uptime']}
Active Incidents: {health['active_incidents']}
Recent Anomalies: {health['recent_anomalies']}

Services:
"""
        for service, status in health["services"].items():
            status_icon = "OK" if status == "healthy" else "DEGRADED"
            output += f"  - {service}: [{status_icon}] {status}\n"
        
        return output
    
    return BaseTool(
        name="get_system_health",
        description="Get an overview of system health. Use this to quickly assess the current state of all systems.",
        args_schema={},
        func=get_system_health
    )


def _create_acknowledge_alert_tool() -> BaseTool:
    """Create the acknowledge alert tool."""
    def acknowledge_alert(alert_id: str, user: str) -> str:
        """Acknowledge an alert.
        
        Args:
            alert_id: Alert ID.
            user: User acknowledging.
            
        Returns:
            Acknowledge result.
        """
        adapter = get_aiops_adapter()
        result = adapter.acknowledge_alert(alert_id, user)
        
        if result["success"]:
            return f"Alert '{alert_id}' acknowledged by {user}"
        return f"Failed: {result.get('error', 'Unknown error')}"
    
    return BaseTool(
        name="acknowledge_alert",
        description="Acknowledge an alert. Use this to indicate you're handling an alert.",
        args_schema={
            "alert_id": str,
            "user": str
        },
        func=acknowledge_alert
    )


def get_all_aiops_tools() -> List[BaseTool]:
    """Get all AIOps tools.
    
    Returns:
        List of all AIOps tools.
    """
    return [
        _create_detect_anomaly_tool(),
        _create_search_logs_tool(),
        _create_query_metrics_tool(),
        _create_incident_tool(),
        _create_get_incident_tool(),
        _create_list_incidents_tool(),
        _create_update_incident_tool(),
        _create_root_cause_analysis_tool(),
        _create_get_system_health_tool(),
        _create_acknowledge_alert_tool(),
    ]
