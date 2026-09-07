import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { authApi } from '../api/auth';
import { User, AuthResponse } from '../types';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

function getDashboardPath(role?: string): string {
  const r = (role || 'USER').toUpperCase();
  if (r === 'ADMIN') return '/admin/dashboard';
  if (r === 'REVIEWER') return '/reviewer/dashboard';
  return '/dashboard';
}

export const useAuth = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [loginError, setLoginError] = useState<string | null>(null);
  const [registerError, setRegisterError] = useState<string | null>(null);

  // Get current user
  const { data: user, isLoading } = useQuery<User | null>({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) return null;
      try {
        return await authApi.me();
      } catch {
        return null;
      }
    },
    retry: false,
    staleTime: 5 * 60 * 1000,
  });

  // Login mutation
  const loginMutation = useMutation({
    mutationFn: (params: { email: string; password: string }) =>
      authApi.login(params.email, params.password),
    onSuccess: (data: AuthResponse) => {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      queryClient.invalidateQueries({ queryKey: ['auth', 'me'] });
      setLoginError(null);
      // Redirect based on role
      const userRole = data.roles?.[0] || 'USER';
      navigate(getDashboardPath(userRole), { replace: true });
    },
    onError: (error: any) => {
      const msg = error?.response?.data?.message || error?.message || 'Invalid email/username or password.';
      setLoginError(msg);
    },
  });

  // Register mutation
  const registerMutation = useMutation({
    mutationFn: (userData: { username: string; email: string; password: string; fullName: string }) =>
      authApi.register(userData),
    onSuccess: () => {
      setRegisterError(null);
      navigate('/login', { replace: true });
    },
    onError: (error: any) => {
      const msg = error?.response?.data?.message || error?.message || 'Registration failed. Please try again.';
      setRegisterError(msg);
    },
  });

  // Logout
  const logout = () => {
    setLoginError(null);
    setRegisterError(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    queryClient.clear();
    navigate('/login');
  };

  return {
    user,
    isLoading,
    login: loginMutation.mutate,
    loginError,
    loginLoading: loginMutation.isPending,
    register: registerMutation.mutate,
    registerError,
    registerLoading: registerMutation.isPending,
    logout,
  };
};
