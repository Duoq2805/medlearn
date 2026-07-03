import { useQuery } from '@tanstack/react-query';
import {
  getUserStreak,
  getUserGoals,
  getUserProgress,
  getRecommendedCases,
} from '../services/apiService';

export const useUserStreak = () => {
  return useQuery({
    queryKey: ['user', 'streak'],
    queryFn: getUserStreak,
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
};

export const useUserGoals = () => {
  return useQuery({
    queryKey: ['user', 'goals'],
    queryFn: getUserGoals,
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
};

export const useUserProgress = () => {
  return useQuery({
    queryKey: ['user', 'progress'],
    queryFn: getUserProgress,
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
};

export const useRecommendedCases = () => {
  return useQuery({
    queryKey: ['cases', 'recommended'],
    queryFn: getRecommendedCases,
    staleTime: 10 * 60 * 1000,
    retry: false,
  });
};
