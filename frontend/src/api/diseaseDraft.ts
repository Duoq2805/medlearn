import apiClient from './client';
import { documentApi } from './document';
import type { DocumentResponse } from './document';
import type { 
  CreateDraftRequest, 
  UpdateDraftRequest, 
  DraftReviewRequest, 
  DiseaseDraftResponse, 
  DraftGenerationResponse 
} from '../types/diseaseDraft';
import type { PageResponse, ApiResponse } from '../types/api';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

import { aiApi } from './ai';
import type { AiDraftRequest } from '../types/ai';

export const uploadDocument = async (file: File): Promise<DocumentResponse> => documentApi.upload({ file });
export const importUrl = async (url: string): Promise<DocumentResponse> => documentApi.importUrl({ url });
export const generateAiDraft = async (request: AiDraftRequest): Promise<DiseaseDraftResponse> => aiApi.generateAiDraft(request);
export const createDiseaseDraft = async (request: { name: string; slug: string; categoryId?: number | null; sections?: Array<{ sectionTypeId?: number | null; title: string; content: string; orderIndex?: number }> }) => {
  const response = await apiClient.post<ApiResponse<any>>('/diseases/draft', request);
  return unwrap(response);
};

export const draftApi = {
  // Create draft manually
  createDraft: async (request: CreateDraftRequest): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>('/drafts', request);
    return unwrap(response);
  },

  // List drafts with optional diseaseId filter
  listDrafts: async (diseaseId?: number, page: number = 0, size: number = 20): Promise<PageResponse<DiseaseDraftResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<DiseaseDraftResponse>>>('/drafts', {
      params: { diseaseId, page, size },
    });
    return unwrap(response);
  },

  // Get draft by ID
  getDraft: async (id: number): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}`);
    return unwrap(response);
  },

  // Update draft content
  updateDraft: async (id: number, request: UpdateDraftRequest): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.put<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}`, request);
    return unwrap(response);
  },

  // Delete draft
  deleteDraft: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/drafts/${id}`);
  },

  // Submit draft for review
  submitDraft: async (id: number): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}/submit`);
    return unwrap(response);
  },

  // Approve draft
  approveDraft: async (id: number, request: DraftReviewRequest): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}/approve`, request);
    return unwrap(response);
  },

  // Reject draft
  rejectDraft: async (id: number, request: DraftReviewRequest): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}/reject`, request);
    return unwrap(response);
  },

  // Archive draft
  archiveDraft: async (id: number): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}/archive`);
    return unwrap(response);
  },

  // Clone draft
  cloneDraft: async (id: number): Promise<DiseaseDraftResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseDraftResponse>>(`/drafts/${id}/clone`);
    return unwrap(response);
  },

  // Apply approved draft to disease version
  applyDraft: async (id: number): Promise<void> => {
    await apiClient.post<ApiResponse<void>>(`/drafts/${id}/apply`);
  },
};
