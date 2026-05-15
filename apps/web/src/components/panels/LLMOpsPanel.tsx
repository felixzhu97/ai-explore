import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

export function LLMOpsPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.llmops}
      description={t.agents.descriptions.llmops}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.llmops, description: t.agents.descriptions.llmops }}
        apiEndpoint="/api/agents/llmops/invoke"
        quickPrompts={t.agents.quickPrompts.llmops}
      />
    </AgentPanel>
  );
}
