import apiClient from './client';
import type { ApiResponse } from '../types/api';
import type { DiseaseDraftResponse } from '../types/diseaseDraft';
import type {
  AiDraftRequest,
  SummaryRequest,
  SummaryResponse,
  SummaryType,
} from '../types/ai';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const aiApi = {
  generateAiDraft: async (request: AiDraftRequest): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>('/ai/drafts', request);
    return unwrap(response);
  },

  generateSummary: async (diseaseId: number, request: SummaryRequest): Promise<SummaryResponse> => {
    const response = await apiClient.post<ApiResponse<SummaryResponse>>(`/ai/summaries/${diseaseId}`, request);
    return unwrap(response);
  },

  listSummaries: async (diseaseId: number): Promise<SummaryResponse[]> => {
    const response = await apiClient.get<ApiResponse<SummaryResponse[]>>(`/ai/summaries/${diseaseId}`);
    return unwrap(response);
  },

  getLatestSummary: async (diseaseId: number, type?: SummaryType): Promise<SummaryResponse> => {
    const response = await apiClient.get<ApiResponse<SummaryResponse>>(`/ai/summaries/${diseaseId}/latest`, {
      params: type ? { type } : undefined,
    });
    return unwrap(response);
  },

  getSummaryByVersion: async (diseaseId: number, version: number, type?: SummaryType): Promise<SummaryResponse> => {
    const response = await apiClient.get<ApiResponse<SummaryResponse>>(`/ai/summaries/${diseaseId}/versions/${version}`, {
      params: type ? { type } : undefined,
    });
    return unwrap(response);
  },
};
