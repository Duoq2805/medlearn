import apiClient from './client';
import type { 
  CreateDiseaseRequest, 
  UpdateDiseaseRequest, 
  DiseaseSearchRequest, 
  DiseaseResponse, 
  DiseaseDetailResponse, 
  DiseaseSummaryProjection 
} from '../types/disease';
import type { DiseaseVersionResponse, CreateDiseaseVersionRequest, UpdateDiseaseVersionRequest, PagedResponse } from '../types/diseaseVersion';
import type { PageResponse, ApiResponse } from '../types/api';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const diseaseApi = {
  // Get drafts belonging to current authenticated user
  getMyDrafts: async (page: number = 0, size: number = 100) => {
    const response = await apiClient.get<ApiResponse<any>>('/drafts', { params: { page, size } });
    return unwrap(response);
  },

  // Fetch all diseases with pagination & filters
  fetchDiseases: async (keyword?: string, categoryId?: number, symptomIds?: number[], page: number = 0, size: number = 20): Promise<PageResponse<DiseaseSummaryProjection>> => {
    const params: Record<string, any> = { page, size };
    if (keyword) params.keyword = keyword;
    if (categoryId) params.categoryId = categoryId;
    if (symptomIds && symptomIds.length > 0) params.symptomIds = symptomIds.join(',');
    const response = await apiClient.get<ApiResponse<PageResponse<DiseaseSummaryProjection>>>('/diseases', { params });
    return unwrap(response);
  },

  // Fetch single disease by ID
  fetchDisease: async (id: string | number): Promise<DiseaseResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseResponse>>(`/diseases/${id}`);
    return unwrap(response);
  },

  // Fetch disease by slug
  fetchDiseaseBySlug: async (slug: string): Promise<DiseaseDetailResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseDetailResponse>>(`/diseases/slug/${slug}`);
    return unwrap(response);
  },

  // Search diseases by name/keyword
  searchDiseases: async (query: string, page: number = 0, size: number = 20): Promise<PageResponse<DiseaseSummaryProjection>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<DiseaseSummaryProjection>>>('/diseases/search', {
      params: { keyword: query, page, size },
    });
    return unwrap(response);
  },

  // Advanced search with body request
  advancedSearch: async (request: DiseaseSearchRequest, page: number = 0, size: number = 20): Promise<PageResponse<DiseaseSummaryProjection>> => {
    const response = await apiClient.post<ApiResponse<PageResponse<DiseaseSummaryProjection>>>('/diseases/search', request, {
      params: { page, size },
    });
    return unwrap(response);
  },

  // Get all disease categories
  getCategories: async () => {
    const response = await apiClient.get<ApiResponse<any>>('/categories');
    return unwrap(response);
  },

  // Create new disease (admin only)
  createDisease: async (disease: CreateDiseaseRequest): Promise<DiseaseResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseResponse>>('/diseases', disease);
    return unwrap(response);
  },

  // Update disease metadata
  updateDisease: async (id: string | number, disease: UpdateDiseaseRequest): Promise<DiseaseResponse> => {
    const response = await apiClient.put<ApiResponse<DiseaseResponse>>(`/diseases/${id}`, disease);
    return unwrap(response);
  },

  // Update disease metadata (alias for draft editing compatibility)
  updateDiseaseMetadata: async (id: string | number, metadata: UpdateDiseaseRequest): Promise<DiseaseResponse> => {
    const response = await apiClient.put<ApiResponse<DiseaseResponse>>(`/diseases/${id}`, metadata);
    return unwrap(response);
  },

  // Clone current approved version into a new draft
  cloneCurrentVersion: async (id: string | number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(`/diseases/${id}/clone-current-version`);
    return unwrap(response);
  },

  // Delete disease (admin only)
  deleteDisease: async (id: string | number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/diseases/${id}`);
  },

  // Restore soft-deleted disease
  restoreDisease: async (id: string | number): Promise<void> => {
    await apiClient.patch<ApiResponse<void>>(`/diseases/${id}/restore`);
  },

  // Assign category to disease
  assignCategory: async (id: string | number, categoryId: number): Promise<void> => {
    await apiClient.patch<ApiResponse<void>>(`/diseases/${id}/category/${categoryId}`);
  },

  // Remove category from disease
  removeCategory: async (id: string | number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/diseases/${id}/category`);
  },

  // Get latest draft version for a disease
  getLatestDraftVersion: async (diseaseId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseVersionResponse>>(`/versions/disease/${diseaseId}/latest-draft`);
    return unwrap(response);
  },

  // Get disease current approved version detail (for DiseaseDetailPage)
  fetchDiseaseDetail: async (id: string | number): Promise<DiseaseDetailResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseDetailResponse>>(`/diseases/${id}/current-version`);
    return unwrap(response);
  },

  // Submit a version for review
  submitVersionForReview: async (versionId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(`/versions/${versionId}/submit`);
    return unwrap(response);
  },

  // Approve a version (reviewer only)
  approveVersion: async (versionId: number, note: string | null = null): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(`/versions/${versionId}/approve`, { note });
    return unwrap(response);
  },

  // Reject a version (reviewer only)
  rejectVersion: async (versionId: number, note: string | null = null): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(`/versions/${versionId}/reject`, { note });
    return unwrap(response);
  },

  // Get versions pending review (for reviewer queue)
  getPendingReviewVersions: async (page: number = 0, size: number = 20): Promise<PagedResponse<DiseaseVersionResponse>> => {
    const response = await apiClient.get<ApiResponse<PagedResponse<DiseaseVersionResponse>>>(`/versions/pending-review`, {
      params: { page, size },
    });
    return unwrap(response);
  },

  // VERSION LIFECYCLE - NEW ENDPOINTS
  createDraftVersion: async (diseaseId: number, request: CreateDiseaseVersionRequest): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(
      '/versions/draft',
      request,
      { params: { diseaseId } }
    );
    return unwrap(response);
  },

  cloneVersion: async (diseaseId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(
      `/versions/clone/${diseaseId}`
    );
    return unwrap(response);
  },

  getVersionById: async (versionId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseVersionResponse>>(
      `/versions/${versionId}`
    );
    return unwrap(response);
  },

  getVersionsByDisease: async (diseaseId: number): Promise<DiseaseVersionResponse[]> => {
    const response = await apiClient.get<ApiResponse<DiseaseVersionResponse[]>>(
      `/versions/disease/${diseaseId}`
    );
    return unwrap(response);
  },

  getCurrentVersion: async (diseaseId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.get<ApiResponse<DiseaseVersionResponse>>(
      `/versions/disease/${diseaseId}/current`
    );
    return unwrap(response);
  },

  updateVersion: async (versionId: number, request: UpdateDiseaseVersionRequest): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.put<ApiResponse<DiseaseVersionResponse>>(
      `/versions/${versionId}`,
      request
    );
    return unwrap(response);
  },

  archiveVersion: async (versionId: number): Promise<DiseaseVersionResponse> => {
    const response = await apiClient.post<ApiResponse<DiseaseVersionResponse>>(
      `/versions/${versionId}/archive`
    );
    return unwrap(response);
  },

  deleteVersion: async (versionId: number): Promise<void> => {
    await apiClient.delete<ApiResponse<null>>(
      `/versions/${versionId}`
    );
  },

  restoreVersion: async (versionId: number): Promise<void> => {
    await apiClient.patch<ApiResponse<null>>(
      `/versions/${versionId}/restore`
    );
  },
};
