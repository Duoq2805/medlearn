import apiClient from './client';
import { CaseStudy } from '../types';

export const caseApi = {
  // Fetch all case studies
  fetchCases: async (page: number = 0, size: number = 20) => {
    const response = await apiClient.get<any>('/cases', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Fetch single case study
  fetchCase: async (id: string) => {
    const response = await apiClient.get<any>(`/cases/${id}`);
    return response.data.data;
  },

  // Create new case study
  createCase: async (caseData: any) => {
    const response = await apiClient.post<any>('/cases', caseData);
    return response.data.data;
  },

  // Submit diagnosis answer for case study
  submitDiagnosis: async (caseId: string, diagnosis: string) => {
    const response = await apiClient.post<any>(
      `/cases/${caseId}/diagnose`,
      { diagnosis }
    );
    return response.data.data;
  },
};
