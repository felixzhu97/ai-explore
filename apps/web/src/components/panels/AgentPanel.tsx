import React from 'react';
import styled from '@emotion/styled';
import { colors, radius, spacing, typography } from '../../theme';

const PanelContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${spacing.md};
`;

const PanelHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: ${spacing.md};
  background: ${colors.surface};
  border: 1px solid ${colors.border};
  border-radius: ${radius.lg};
`;

const PanelTitle = styled.h3`
  font-size: ${typography.fontSize.lg};
  font-weight: ${typography.fontWeight.semibold};
  color: ${colors.text};
  margin: 0;
`;

const PanelDescription = styled.p`
  font-size: ${typography.fontSize.sm};
  color: ${colors.textSecondary};
  margin: 0;
  margin-top: ${spacing.xs};
`;

const PanelContent = styled.div`
  background: ${colors.surface};
  border: 1px solid ${colors.border};
  border-radius: ${radius.lg};
  padding: ${spacing.lg};
`;

interface AgentPanelProps {
  title: string;
  description: string;
  children: React.ReactNode;
  headerRight?: React.ReactNode;
  hideHeader?: boolean;
}

export function AgentPanel({ title, description, children, headerRight, hideHeader }: AgentPanelProps) {
  return (
    <PanelContainer>
      {!hideHeader && (
        <PanelHeader>
          <div>
            <PanelTitle>{title}</PanelTitle>
            <PanelDescription>{description}</PanelDescription>
          </div>
          {headerRight && <div>{headerRight}</div>}
        </PanelHeader>
      )}
      <PanelContent>
        {children}
      </PanelContent>
    </PanelContainer>
  );
}
