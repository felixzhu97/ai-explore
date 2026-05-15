import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

export function SupervisorPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.supervisor}
      description={t.agents.descriptions.supervisor}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.supervisor, description: t.agents.descriptions.supervisor }}
        apiEndpoint="/api/agents/supervisor/invoke"
        quickPrompts={t.agents.quickPrompts.supervisor}
      />
    </AgentPanel>
  );
}
