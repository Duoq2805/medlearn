import apiClient from './client';
import { User } from '../types';

export const adminApi = {
  // Get all users
  fetchUsers: async (page: number = 0, size: number = 50) => {
    const response = await apiClient.get<any>('/admin/users', {
      params: { page, size },
    });
    return response.data.data;
  },

  // Update user role
  updateUserRole: async (userId: string, role: 'student' | 'reviewer' | 'admin') => {
    const response = await apiClient.patch<any>(`/admin/users/${userId}/role`, { roleName: role });
    return response.data.data;
  },

  // Deactivate/activate user
  toggleUserStatus: async (userId: string, isActive: boolean) => {
    const response = await apiClient.patch<any>(`/admin/users/${userId}/status`, { isActive });
    return response.data.data;
  },

  // Get system analytics
  fetchAnalytics: async () => {
    const response = await apiClient.get<any>('/admin/analytics');
    return response.data.data;
  },

  // Get pending reviews queue
  fetchPendingReviews: async (page: number = 0, size: number = 20) => {
    const response = await apiClient.get<any>('/admin/pending-reviews', {
      params: { page, size },
    });
    return response.data.data;
  },
};
