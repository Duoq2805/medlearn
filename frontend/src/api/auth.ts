import apiClient from './client';

export const authApi = {
  // Register a new user
  register: async (userData: { username: string; email: string; password: string; fullName: string }) => {
    const response = await apiClient.post<any>('/auth/register', userData);
    return response.data.data;
  },

  // Login user
  login: async (email: string, password: string) => {
    const response = await apiClient.post<any>('/auth/login', { usernameOrEmail: email, password });
    return response.data.data;
  },

  // Request password reset
  forgotPassword: async (email: string) => {
    const response = await apiClient.post<any>('/auth/forgot-password', { email });
    return response.data.data;
  },

  // Verify email
  verifyEmail: async (token: string) => {
    const response = await apiClient.get<any>(`/auth/verify?token=${token}`);
    return response.data.data;
  },

  // Get current logged in user
  me: async () => {
    const response = await apiClient.get<any>('/auth/me');
    return response.data.data;
  },

  // Logout user
  logout: async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await apiClient.post<any>('/auth/logout', refreshToken ? { refreshToken } : {});
    return response.data.data;
  },

  // Refresh token
  refresh: async (refreshToken: string) => {
    const response = await apiClient.post<any>('/auth/refresh', { refreshToken });
    return response.data.data;
  },
};
