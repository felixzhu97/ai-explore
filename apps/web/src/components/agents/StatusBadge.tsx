import styled from '@emotion/styled';
import { css } from '@emotion/react';
import { colors, radius, typography } from '../../theme';

type BadgeStatus = 'online' | 'offline' | 'busy' | 'error' | 'pending';

interface StatusBadgeProps {
  status: BadgeStatus;
  label?: string;
  showDot?: boolean;
}

const Badge = styled.span<{ status: BadgeStatus }>`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  font-size: ${typography.fontSize.xs};
  font-weight: ${typography.fontWeight.medium};
  border-radius: ${radius.full};
  white-space: nowrap;

  ${({ status }) => {
    switch (status) {
      case 'online':
        return css`
          background: ${colors.successLight};
          color: ${colors.success};
        `;
      case 'offline':
        return css`
          background: rgba(0, 0, 0, 0.06);
          color: ${colors.textTertiary};
        `;
      case 'busy':
        return css`
          background: ${colors.warningLight};
          color: ${colors.warning};
        `;
      case 'error':
        return css`
          background: ${colors.errorLight};
          color: ${colors.error};
        `;
      case 'pending':
        return css`
          background: ${colors.primaryLight};
          color: ${colors.primary};
        `;
    }
  }}
`;

const Dot = styled.span<{ status: BadgeStatus }>`
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
`;

const statusLabels: Record<BadgeStatus, string> = {
  online: 'Online',
  offline: 'Offline',
  busy: 'Busy',
  error: 'Error',
  pending: 'Pending',
};

export function StatusBadge({ status, label, showDot = true }: StatusBadgeProps) {
  return (
    <Badge status={status}>
      {showDot && <Dot status={status} />}
      {label || statusLabels[status]}
    </Badge>
  );
}
