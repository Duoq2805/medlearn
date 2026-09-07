import apiClient from './client';
import type { User } from '../types';

export interface UpdateProfileRequest {
  username?: string;
  email?: string;
  fullName?: string;
  avatarUrl?: string;
  phoneNumber?: string;
}

export const userApi = {
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get<{ data: User }>('/users/me');
    return response.data.data;
  },

  updateProfile: async (request: UpdateProfileRequest): Promise<User> => {
    const response = await apiClient.put<{ data: User }>('/users/me', request);
    return response.data.data;
  },
};
