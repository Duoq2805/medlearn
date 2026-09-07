import apiClient from './client';
import type { RoleRequest, UserStatusRequest, AnalyticsResponse } from '../types/admin';
import type { UserProfileResponse } from '../types/user';
import type { PageResponse, ApiResponse } from '../types/api';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const adminApi = {
  // Get all users
  fetchUsers: async (page: number = 0, size: number = 20): Promise<PageResponse<UserProfileResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<UserProfileResponse>>>('/admin/users', {
      params: { page, size },
    });
    return unwrap(response);
  },

  // Get user by ID
  getUserById: async (id: number): Promise<UserProfileResponse> => {
    const response = await apiClient.get<ApiResponse<UserProfileResponse>>(`/admin/users/${id}`);
    return unwrap(response);
  },

  // Update user role ('ADMIN' | 'REVIEWER' | 'USER')
  updateUserRole: async (userId: string | number, roleName: string): Promise<void> => {
    const request: RoleRequest = { roleName };
    await apiClient.patch<ApiResponse<void>>(`/admin/users/${userId}/role`, request);
  },

  // Deactivate/activate user
  toggleUserStatus: async (userId: string | number, isActive: boolean): Promise<void> => {
    const request: UserStatusRequest = { isActive };
    await apiClient.patch<ApiResponse<void>>(`/admin/users/${userId}/status`, request);
  },

  // Get system analytics
  fetchAnalytics: async (): Promise<AnalyticsResponse> => {
    const response = await apiClient.get<ApiResponse<AnalyticsResponse>>('/admin/analytics');
    return unwrap(response);
  },

  // Get pending reviews notice message (Note: Full list is GET /versions/pending-review)
  fetchPendingReviews: async (): Promise<string> => {
    const response = await apiClient.get<ApiResponse<string>>('/admin/pending-reviews');
    return unwrap(response);
  },
};
