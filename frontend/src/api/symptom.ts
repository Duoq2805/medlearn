import apiClient from './client';

export const symptomApi = {
  // Fetch all symptoms
  fetchSymptoms: async () => {
    const response = await apiClient.get<any>('/symptoms');
    return response.data.data;
  },

  // Search symptoms (for autocomplete)
  searchSymptoms: async (query: string) => {
    const response = await apiClient.get<any>('/symptoms/search', {
      params: { keyword: query },
    });
    return response.data.data;
  },

  // Get disease recommendations based on selected symptoms
  getDiseaseRecommendations: async (symptomIds: number[]) => {
    const response = await apiClient.post<any>('/symptom-checker/check', {
      symptomIds,
      limit: 10,
    });
    return response.data.data;
  },

  // Create new symptom (admin only)
  createSymptom: async (symptom: any) => {
    const response = await apiClient.post<any>('/symptoms', symptom);
    return response.data.data;
  },

  // Update symptom (admin only)
  updateSymptom: async (id: string, symptom: any) => {
    const response = await apiClient.put<any>(`/symptoms/${id}`, symptom);
    return response.data.data;
  },

  // Delete symptom (admin only)
  deleteSymptom: async (id: string) => {
    const response = await apiClient.delete<any>(`/symptoms/${id}`);
    return response.data.data;
  },
};
