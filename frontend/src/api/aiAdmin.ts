import apiClient from './client';
import type { ApiResponse } from '../types/api';
import type {
  PromptTemplateResponse,
  PromptTestResultResponse,
  ModelInfo,
  AiMessage,
} from '../types/aiAdmin';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const aiAdminApi = {
  listPrompts: async (): Promise<PromptTemplateResponse[]> => {
    const response = await apiClient.get<ApiResponse<PromptTemplateResponse[]>>('/ai/admin/prompts');
    return unwrap(response);
  },

  getPrompt: async (code: string, version?: string): Promise<PromptTemplateResponse> => {
    const response = await apiClient.get<ApiResponse<PromptTemplateResponse>>(`/ai/admin/prompts/${code}`, {
      params: version ? { version } : undefined,
    });
    return unwrap(response);
  },

  createPrompt: async (dto: Partial<PromptTemplateResponse>): Promise<PromptTemplateResponse> => {
    const response = await apiClient.post<ApiResponse<PromptTemplateResponse>>('/ai/admin/prompts', dto);
    return unwrap(response);
  },

  updatePrompt: async (id: number, dto: Partial<PromptTemplateResponse>): Promise<PromptTemplateResponse> => {
    const response = await apiClient.put<ApiResponse<PromptTemplateResponse>>(`/ai/admin/prompts/${id}`, dto);
    return unwrap(response);
  },

  deletePrompt: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/ai/admin/prompts/${id}`);
  },

  listVersions: async (code: string): Promise<PromptTemplateResponse[]> => {
    const response = await apiClient.get<ApiResponse<PromptTemplateResponse[]>>(`/ai/admin/prompts/${code}/versions`);
    return unwrap(response);
  },

  validatePrompt: async (systemPrompt: string, userPromptTemplate: string): Promise<string[]> => {
    const response = await apiClient.post<ApiResponse<string[]>>('/ai/admin/prompts/validate', {
      systemPrompt,
      userPromptTemplate,
    });
    return unwrap(response);
  },

  extractVariables: async (template: string): Promise<{ variables: string[] }> => {
    const response = await apiClient.post<ApiResponse<{ variables: string[] }>>('/ai/admin/prompts/extract-variables', {
      template,
    });
    return unwrap(response);
  },

  testPrompt: async (body: {
    code: string;
    version?: string;
    variables?: Record<string, string>;
    expectedOutput?: string;
    model?: string;
  }): Promise<PromptTestResultResponse> => {
    const response = await apiClient.post<ApiResponse<PromptTestResultResponse>>('/ai/admin/prompts/test', body);
    return unwrap(response);
  },

  getTestResults: async (code: string): Promise<PromptTestResultResponse[]> => {
    const response = await apiClient.get<ApiResponse<PromptTestResultResponse[]>>(`/ai/admin/prompts/${code}/test-results`);
    return unwrap(response);
  },

  listModels: async (provider?: string): Promise<Record<string, ModelInfo[]>> => {
    const response = await apiClient.get<ApiResponse<Record<string, ModelInfo[]>>>('/ai/admin/models', {
      params: provider ? { provider } : undefined,
    });
    return unwrap(response);
  },

  buildPrompt: async (body: {
    code: string;
    version?: string;
    variables?: Record<string, string>;
  }): Promise<AiMessage[]> => {
    const response = await apiClient.post<ApiResponse<AiMessage[]>>('/ai/admin/prompts/build', body);
    return unwrap(response);
  },
};
