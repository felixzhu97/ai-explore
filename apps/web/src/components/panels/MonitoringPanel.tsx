import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

const QUICK_PROMPTS = [
  'Show CPU usage for the last hour',
  'Any alerts triggered today?',
  'Compare latency across services',
];

export function MonitoringPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.monitoring}
      description={t.agents.descriptions.monitoring}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.monitoring, description: t.agents.descriptions.monitoring }}
        apiEndpoint="/api/agents/monitoring/invoke"
        quickPrompts={QUICK_PROMPTS}
      />
    </AgentPanel>
  );
}
