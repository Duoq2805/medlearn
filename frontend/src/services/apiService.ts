import axios from 'axios';
import {
  USE_MOCK_DATA,
  mockStreakData,
  mockGoalsData,
  mockProgressData,
  mockRecommendedCases,
  type StreakData,
  type GoalsData,
  type ProgressData,
  type RecommendedCase,
} from './mockData';

const api = axios.create({
  baseURL: 'http://localhost:6060/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// ============================================
// USER STREAK
// TODO: Replace with real API when available
// Endpoint: GET /api/user/streak (not yet implemented)
// ============================================
export const getUserStreak = async (): Promise<StreakData> => {
  if (USE_MOCK_DATA) {
    // 🔄 MOCK DATA - Remove this block when backend is ready
    console.warn('🔁 Using MOCK data for /user/streak');
    return mockStreakData;
  }
  const response = await api.get('/user/streak');
  return response.data;
};

// ============================================
// USER GOALS
// TODO: Replace with real API when available
// Endpoint: GET /api/user/goals (not yet implemented)
// ============================================
export const getUserGoals = async (): Promise<GoalsData> => {
  if (USE_MOCK_DATA) {
    // 🔄 MOCK DATA - Remove this block when backend is ready
    console.warn('🔁 Using MOCK data for /user/goals');
    return mockGoalsData;
  }
  const response = await api.get('/user/goals');
  return response.data;
};

// ============================================
// USER PROGRESS
// TODO: Replace with real API when available
// Endpoint: GET /api/user/progress (not yet implemented)
// ============================================
export const getUserProgress = async (): Promise<ProgressData> => {
  if (USE_MOCK_DATA) {
    // 🔄 MOCK DATA - Remove this block when backend is ready
    console.warn('🔁 Using MOCK data for /user/progress');
    return mockProgressData;
  }
  const response = await api.get('/user/progress');
  return response.data;
};

// ============================================
// RECOMMENDED CASES
// TODO: Replace with real API when available
// Endpoint: GET /api/cases/recommended (not yet implemented)
// ============================================
export const getRecommendedCases = async (): Promise<RecommendedCase[]> => {
  if (USE_MOCK_DATA) {
    // 🔄 MOCK DATA - Remove this block when backend is ready
    console.warn('🔁 Using MOCK data for /cases/recommended');
    return mockRecommendedCases;
  }
  const response = await api.get('/cases/recommended');
  return response.data;
};
