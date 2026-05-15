import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

export function ModelPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.model}
      description={t.agents.descriptions.model}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.model, description: t.agents.descriptions.model }}
        apiEndpoint="/api/agents/model/invoke"
        quickPrompts={t.agents.quickPrompts.model}
      />
    </AgentPanel>
  );
}
