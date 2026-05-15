import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

export function AIOpsPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.aiops}
      description={t.agents.descriptions.aiops}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.aiops, description: t.agents.descriptions.aiops }}
        apiEndpoint="/api/agents/aiops/invoke"
        quickPrompts={t.agents.quickPrompts.aiops}
      />
    </AgentPanel>
  );
}
