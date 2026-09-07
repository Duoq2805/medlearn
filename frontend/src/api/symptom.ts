import apiClient from './client';
import type { 
  SymptomResponse, 
  CreateSymptomRequest, 
  UpdateSymptomRequest, 
  SymptomCheckerRequest, 
  SymptomMatchResult, 
  SymptomAnalysisResponse, 
  SymptomAnalysisV2Response 
} from '../types/symptom';
import type { ApiResponse } from '../types/api';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const symptomApi = {
  // Fetch all symptoms
  fetchSymptoms: async (): Promise<SymptomResponse[]> => {
    const response = await apiClient.get<ApiResponse<SymptomResponse[]>>('/symptoms');
    return unwrap(response);
  },

  // Get symptom by ID
  getSymptomById: async (id: number): Promise<SymptomResponse> => {
    const response = await apiClient.get<ApiResponse<SymptomResponse>>(`/symptoms/${id}`);
    return unwrap(response);
  },

  // Search symptoms by keyword
  searchSymptoms: async (query: string): Promise<SymptomResponse[]> => {
    const response = await apiClient.get<ApiResponse<SymptomResponse[]>>('/symptoms/search', {
      params: { keyword: query },
    });
    return unwrap(response);
  },

  // Create new symptom (admin only)
  createSymptom: async (symptom: CreateSymptomRequest): Promise<SymptomResponse> => {
    const response = await apiClient.post<ApiResponse<SymptomResponse>>('/symptoms', symptom);
    return unwrap(response);
  },

  // Update symptom (admin only)
  updateSymptom: async (id: string | number, symptom: UpdateSymptomRequest): Promise<SymptomResponse> => {
    const response = await apiClient.put<ApiResponse<SymptomResponse>>(`/symptoms/${id}`, symptom);
    return unwrap(response);
  },

  // Delete symptom (admin only)
  deleteSymptom: async (id: string | number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/symptoms/${id}`);
  },

  // Symptom Checker V0 / Check
  checkSymptomsV0: async (symptomIds: number[], limit: number = 10): Promise<SymptomMatchResult[]> => {
    const request: SymptomCheckerRequest = { symptomIds, limit };
    const response = await apiClient.post<ApiResponse<SymptomMatchResult[]>>('/symptom-checker/check', request);
    return unwrap(response);
  },

  // Alias for backward compatibility
  getDiseaseRecommendations: async (symptomIds: number[]): Promise<SymptomMatchResult[]> => {
    const request: SymptomCheckerRequest = { symptomIds, limit: 10 };
    const response = await apiClient.post<ApiResponse<SymptomMatchResult[]>>('/symptom-checker/check', request);
    return unwrap(response);
  },

  // Symptom Checker V1 / Analyze
  analyzeSymptomsV1: async (symptomIds: number[], limit: number = 10): Promise<SymptomAnalysisResponse[]> => {
    const request: SymptomCheckerRequest = { symptomIds, limit };
    const response = await apiClient.post<ApiResponse<SymptomAnalysisResponse[]>>('/symptom-checker/analyze', request);
    return unwrap(response);
  },

  // Symptom Checker V2 / Analyze
  analyzeSymptomsV2: async (symptomIds: number[], limit: number = 10): Promise<SymptomAnalysisV2Response[]> => {
    const request: SymptomCheckerRequest = { symptomIds, limit };
    const response = await apiClient.post<ApiResponse<SymptomAnalysisV2Response[]>>('/symptom-checker/v2/analyze', request);
    return unwrap(response);
  },
};
