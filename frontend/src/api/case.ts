import apiClient from './client';
import type { 
  CreateCaseStudyRequest, 
  DiagnoseRequest, 
  DiagnoseResponse, 
  CaseStudyDetailResponse, 
  CaseStudySummaryProjection 
} from '../types/caseStudy';
import type { PageResponse, ApiResponse } from '../types/api';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const caseApi = {
  // Fetch all approved case studies
  fetchCases: async (page: number = 0, size: number = 20): Promise<PageResponse<CaseStudySummaryProjection>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<CaseStudySummaryProjection>>>('/cases', {
      params: { page, size },
    });
    return unwrap(response);
  },

  // Fetch single case study by ID
  fetchCase: async (id: string | number): Promise<CaseStudyDetailResponse> => {
    const response = await apiClient.get<ApiResponse<CaseStudyDetailResponse>>(`/cases/${id}`);
    return unwrap(response);
  },

  // Fetch single case study by slug
  fetchCaseBySlug: async (slug: string): Promise<CaseStudyDetailResponse> => {
    const response = await apiClient.get<ApiResponse<CaseStudyDetailResponse>>(`/cases/slug/${slug}`);
    return unwrap(response);
  },

  // Create new case study
  createCase: async (caseData: CreateCaseStudyRequest): Promise<CaseStudyDetailResponse> => {
    const response = await apiClient.post<ApiResponse<CaseStudyDetailResponse>>('/cases', caseData);
    return unwrap(response);
  },

  // Submit diagnosis answer for case study
  submitDiagnosis: async (caseId: string | number, diagnosis: string): Promise<DiagnoseResponse> => {
    const request: DiagnoseRequest = { diagnosis };
    const response = await apiClient.post<ApiResponse<DiagnoseResponse>>(
      `/cases/${caseId}/diagnose`,
      request
    );
    return unwrap(response);
  },
};
