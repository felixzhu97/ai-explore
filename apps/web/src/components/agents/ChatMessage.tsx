import { css, keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import { colors, radius, spacing, typography } from '../../theme';
import { ToolResult, ToolResultData } from './ToolResult';

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
`;

const MessageBubble = styled.div<{ isUser: boolean }>`
  display: flex;
  flex-direction: column;
  max-width: 80%;
  animation: ${fadeIn} 0.2s ease;
  align-self: ${({ isUser }) => (isUser ? 'flex-end' : 'flex-start')};
  align-items: ${({ isUser }) => (isUser ? 'flex-end' : 'flex-start')};
`;

const MessageContent = styled.div<{ isUser: boolean }>`
  padding: ${spacing.md};
  border-radius: ${radius.lg};
  font-size: ${typography.fontSize.base};
  line-height: ${typography.lineHeight.relaxed};
  word-break: break-word;

  ${({ isUser }) =>
    isUser
      ? css`
          background: ${colors.primary};
          color: white;
          border-bottom-right-radius: ${radius.sm};
        `
      : css`
          background: ${colors.surface};
          border: 1px solid ${colors.border};
          border-bottom-left-radius: ${radius.sm};
          color: ${colors.text};
        `}

  h1, h2, h3, h4, h5, h6 {
    margin: 0.5em 0 0.25em;
    font-weight: 600;
    line-height: 1.3;
  }
  h1:first-child, h2:first-child, h3:first-child, h4:first-child, h5:first-child, h6:first-child {
    margin-top: 0;
  }
  h1:last-child, h2:last-child, h3:last-child, h4:last-child, h5:last-child, h6:last-child {
    margin-bottom: 0;
  }
  h1 { font-size: 1.2em; }
  h2 { font-size: 1.1em; }
  h3 { font-size: 1.05em; }

  p { margin: 0.5em 0; }
  p:first-child { margin-top: 0; }
  p:last-child { margin-bottom: 0; }
  ul, ol { margin: 0.5em 0; padding-left: 1.5em; }
  li { margin: 0.25em 0; }

  code {
    font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
    font-size: 0.9em;
    padding: 0.15em 0.4em;
    border-radius: 3px;
    background: ${colors.surfaceSecondary};
  }
  pre {
    margin: 0.5em 0;
    padding: ${spacing.sm};
    border-radius: ${radius.sm};
    background: ${colors.background};
    overflow-x: auto;
    code { padding: 0; background: none; }
  }

  blockquote {
    margin: 0.5em 0;
    padding-left: 1em;
    border-left: 3px solid ${colors.primary}50;
    color: ${colors.textSecondary};
  }

  table {
    width: 100%;
    border-collapse: collapse;
    margin: 0.5em 0;
    font-size: 0.9em;
  }
  th, td {
    border: 1px solid ${colors.border};
    padding: 0.5em;
    text-align: left;
  }
  th { background: ${colors.surfaceSecondary}; font-weight: 600; }

  strong { font-weight: 600; }

  .hljs { background: ${colors.background}; color: ${colors.text}; }
  .hljs-keyword, .hljs-selector-tag { color: #7c3aed; }
  .hljs-string, .hljs-attr { color: #059669; }
  .hljs-number { color: #dc2626; }
  .hljs-comment { color: ${colors.textTertiary}; font-style: italic; }
  .hljs-function, .hljs-title { color: #2563eb; }
  .hljs-variable, .hljs-params { color: #ea580c; }
`;

const MessageMeta = styled.div`
  display: flex;
  align-items: center;
  gap: ${spacing.sm};
  margin-top: 4px;
  padding: 0 4px;
`;

const MessageTime = styled.span`
  font-size: ${typography.fontSize.xs};
  color: ${colors.textTertiary};
`;

export interface ToolCall {
  id: string;
  name: string;
  input: Record<string, unknown>;
  output?: string;
  status: 'pending' | 'running' | 'success' | 'error';
}

export interface ChatMessageData {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
  toolCalls?: ToolCall[];
}

interface ChatMessageProps {
  message: ChatMessageData;
}

export function ChatMessage({ message }: ChatMessageProps) {
  const isUser = message.role === 'user';

  return (
    <MessageBubble isUser={isUser}>
      <MessageContent isUser={isUser}>
        {isUser ? (
          message.content
        ) : (
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeHighlight]}
          >
            {message.content}
          </ReactMarkdown>
        )}
      </MessageContent>
      
      {message.toolCalls && message.toolCalls.length > 0 && (
        <div style={{ marginTop: spacing.sm, width: '100%', maxWidth: '500px' }}>
          {message.toolCalls.map((toolCall) => (
            <ToolResult
              key={toolCall.id}
              toolCall={toolCall}
            />
          ))}
        </div>
      )}
      
      <MessageMeta>
        <MessageTime>
          {new Date(message.timestamp).toLocaleTimeString()}
        </MessageTime>
      </MessageMeta>
    </MessageBubble>
  );
}
