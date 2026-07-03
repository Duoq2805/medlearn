import apiClient from './client';
import { Disease } from '../types';

export const diseaseApi = {
  // Fetch all diseases with pagination
  fetchDiseases: async (page: number = 0, size: number = 20) => {
    const response = await apiClient.get<any>('/diseases', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Fetch single disease by ID
  fetchDisease: async (id: string) => {
    const response = await apiClient.get<any>(`/diseases/${id}`);
    return response.data.data;
  },

  // Search diseases by name
  searchDiseases: async (query: string) => {
    const response = await apiClient.get<any>('/diseases/search', {
      params: { keyword: query },
    });
    return response.data.data;
  },

  // Get all disease categories
  getCategories: async () => {
    const response = await apiClient.get<any>('/categories');
    return response.data.data;
  },

  // Create new disease (admin only)
  createDisease: async (disease: any) => {
    const response = await apiClient.post<any>('/diseases', disease);
    return response.data.data;
  },

  // Update disease (admin only)
  updateDisease: async (id: string, disease: any) => {
    const response = await apiClient.put<any>(`/diseases/${id}`, disease);
    return response.data.data;
  },

  // Delete disease (admin only)
  deleteDisease: async (id: string) => {
    const response = await apiClient.delete<any>(`/diseases/${id}`);
    return response.data.data;
  },
};
