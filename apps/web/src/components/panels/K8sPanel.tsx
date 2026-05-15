import { AgentChat } from '../agents/AgentChat';
import { AgentPanel } from './AgentPanel';
import { StatusBadge } from '../agents/StatusBadge';
import { useI18n } from '../../i18n';

const AGENT_INFO = {
  status: 'online' as const,
};

const QUICK_PROMPTS = [
  'Show me all running pods',
  'Check cluster health status',
  'Scale deployment to 3 replicas',
];

export function K8sPanel() {
  const { t } = useI18n();
  return (
    <AgentPanel
      title={t.nav.kubernetes}
      description={t.agents.descriptions.k8s}
      headerRight={<StatusBadge status={AGENT_INFO.status} />}
    >
      <AgentChat
        agentInfo={{ name: t.nav.kubernetes, description: t.agents.descriptions.k8s }}
        apiEndpoint="/api/agents/k8s/invoke"
        quickPrompts={QUICK_PROMPTS}
      />
    </AgentPanel>
  );
}
