export interface PromptTemplateResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  systemPrompt?: string;
  userPromptTemplate?: string;
  version?: string;
  model?: string;
  temperature?: number;
  maxTokens?: number;
  requiredVariables?: string[];
  active: boolean;
  status?: string;
}

export interface PromptTestResultResponse {
  id: number;
  promptCode: string;
  version?: string;
  variables?: Record<string, string>;
  expectedOutput?: string;
  actualOutput?: string;
  passed: boolean;
  errorMessage?: string;
  latencyMs?: number;
  executedAt?: string;
}

export interface ModelInfo {
  id: string;
  name: string;
  provider: string;
  capabilities: string[];
  supportsStreaming: boolean;
  supportsFunctions: boolean;
  contextWindow: number;
}

export type AiRole = 'SYSTEM' | 'USER' | 'ASSISTANT' | 'FUNCTION' | 'TOOL';

export interface AiMessage {
  role: AiRole;
  content: string;
}

export interface AiStreamChunkResponse {
  chunk: string;
  done: boolean;
  createdAt?: string;
}
