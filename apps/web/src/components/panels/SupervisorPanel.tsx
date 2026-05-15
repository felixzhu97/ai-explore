import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  name: 'Supervisor Agent',
  status: 'online' as const,
};

const QUICK_PROMPTS = [
  'Deploy a new microservice',
  'Diagnose high latency issue',
  'Generate system report',
];

export function SupervisorPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.supervisor}
      description={t.agents.descriptions.supervisor}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ ...AGENT_INFO, name: t.nav.supervisor, description: t.agents.descriptions.supervisor }}
        apiEndpoint="/api/agents/supervisor/invoke"
        quickPrompts={QUICK_PROMPTS}
      />
    </AgentPanel>
  );
}
