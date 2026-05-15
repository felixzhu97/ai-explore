import { useState } from 'react';
import styled from '@emotion/styled';
import { css, keyframes } from '@emotion/react';
import { colors, radius, spacing, typography } from '../../theme';
import { ToolCall } from './ChatMessage';

const pulse = keyframes`
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
`;

const spin = keyframes`
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
`;

const ToolContainer = styled.div`
  background: ${colors.surfaceSecondary};
  border: 1px solid ${colors.border};
  border-radius: ${radius.md};
  overflow: hidden;
  font-size: ${typography.fontSize.sm};
`;

const ToolHeader = styled.div<{ status: ToolCall['status'] }>`
  display: flex;
  align-items: center;
  gap: ${spacing.sm};
  padding: ${spacing.sm} ${spacing.md};
  background: ${({ status }) => {
    switch (status) {
      case 'error': return colors.errorLight;
      case 'success': return colors.successLight;
      default: return colors.surface;
    }
  }};
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: ${({ status }) => {
      switch (status) {
        case 'error': return colors.errorLight;
        case 'success': return colors.successLight;
        default: return colors.surfaceTertiary;
      }
    }};
  }
`;

const ToolIcon = styled.span`
  font-size: 14px;
`;

const ToolName = styled.span`
  font-weight: ${typography.fontWeight.medium};
  color: ${colors.text};
  flex: 1;
`;

const StatusIndicator = styled.span<{ status: ToolCall['status'] }>`
  ${({ status }) => {
    switch (status) {
      case 'pending':
        return css`color: ${colors.textTertiary};`;
      case 'running':
        return css`
          color: ${colors.warning};
          animation: ${pulse} 1s ease-in-out infinite;
        `;
      case 'success':
        return css`color: ${colors.success};`;
      case 'error':
        return css`color: ${colors.error};`;
    }
  }}
`;

const SpinnerIcon = styled.span`
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: ${spin} 0.6s linear infinite;
`;

const ExpandIcon = styled.span<{ expanded: boolean }>`
  display: inline-block;
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid ${colors.textTertiary};
  transform: rotate(${({ expanded }) => expanded ? '180deg' : '0deg'});
  transition: transform 0.2s ease;
`;

const ToolBody = styled.div<{ expanded: boolean }>`
  display: ${({ expanded }) => expanded ? 'block' : 'none'};
  padding: ${spacing.md};
  border-top: 1px solid ${colors.border};
  max-height: 300px;
  overflow-y: auto;
`;

const SectionLabel = styled.div`
  font-size: ${typography.fontSize.xs};
  color: ${colors.textTertiary};
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: ${spacing.xs};
`;

const CodeBlock = styled.pre`
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: ${typography.fontSize.xs};
  line-height: 1.5;
  color: ${colors.text};
  background: ${colors.background};
  padding: ${spacing.sm};
  border-radius: ${radius.sm};
  overflow-x: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
`;

const ErrorText = styled.div`
  color: ${colors.error};
  font-size: ${typography.fontSize.sm};
`;

const EmptyOutput = styled.div`
  color: ${colors.textTertiary};
  font-style: italic;
`;

interface ToolResultProps {
  toolCall: ToolCall;
}

export function ToolResult({ toolCall }: ToolResultProps) {
  const [expanded, setExpanded] = useState(false);

  const getStatusIcon = () => {
    switch (toolCall.status) {
      case 'pending': return '○';
      case 'running': return <SpinnerIcon />;
      case 'success': return '✓';
      case 'error': return '✗';
    }
  };

  const formatJson = (obj: unknown): string => {
    try {
      return JSON.stringify(obj, null, 2);
    } catch {
      return String(obj);
    }
  };

  return (
    <ToolContainer>
      <ToolHeader
        status={toolCall.status}
        onClick={() => setExpanded(!expanded)}
      >
        <ToolIcon>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
            </svg>
          </ToolIcon>
        <ToolName>{toolCall.name}</ToolName>
        <StatusIndicator status={toolCall.status}>
          {getStatusIcon()}
        </StatusIndicator>
        <ExpandIcon expanded={expanded} />
      </ToolHeader>
      
      <ToolBody expanded={expanded}>
        <SectionLabel>Input</SectionLabel>
        <CodeBlock>{formatJson(toolCall.input)}</CodeBlock>
        
        {toolCall.output && (
          <>
            <SectionLabel style={{ marginTop: spacing.md }}>Output</SectionLabel>
            {toolCall.status === 'error' ? (
              <ErrorText>{toolCall.output}</ErrorText>
            ) : (
              <CodeBlock>{toolCall.output}</CodeBlock>
            )}
          </>
        )}
        
        {!toolCall.output && toolCall.status === 'success' && (
          <EmptyOutput>No output</EmptyOutput>
        )}
      </ToolBody>
    </ToolContainer>
  );
}
