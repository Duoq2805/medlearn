import apiClient from './client';
import type { AuthResponse, User } from '../types';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export const authApi = {
  register: async (userData: RegisterRequest): Promise<void> => {
    await apiClient.post('/auth/register', userData);
  },

  login: async (email: string, password: string): Promise<AuthResponse> => {
    const response = await apiClient.post<{ data: AuthResponse }>('/auth/login', { usernameOrEmail: email, password });
    return response.data.data;
  },

  forgotPassword: async (email: string): Promise<void> => {
    await apiClient.post('/auth/forgot-password', { email });
  },

  resendVerification: async (email: string): Promise<void> => {
    await apiClient.post('/auth/resend-verification', { email });
  },

  resetPassword: async (request: ResetPasswordRequest): Promise<void> => {
    await apiClient.post('/auth/reset-password', request);
  },

  verifyEmail: async (token: string): Promise<void> => {
    await apiClient.get(`/auth/verify?token=${encodeURIComponent(token)}`);
  },

  me: async (): Promise<User> => {
    const response = await apiClient.get<{ data: User }>('/auth/me');
    return response.data.data;
  },

  logout: async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken');
    await apiClient.post('/auth/logout', refreshToken ? { refreshToken } : {});
  },

  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<{ data: AuthResponse }>('/auth/refresh', { refreshToken });
    return response.data.data;
  },
};
