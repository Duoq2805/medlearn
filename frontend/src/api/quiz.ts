import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  QuizGenerateRequest,
  QuizGenerateResponse,
  QuizResponse,
  QuizSubmitRequest,
  QuizSubmitResponse,
  QuizAttemptResponse,
} from '../types/quiz';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const quizApi = {
  generate: async (request: QuizGenerateRequest): Promise<QuizGenerateResponse> => {
    const response = await apiClient.post<ApiResponse<QuizGenerateResponse>>('/quizzes/generate', request);
    return unwrap(response);
  },

  getById: async (quizId: number): Promise<QuizResponse> => {
    const response = await apiClient.get<ApiResponse<QuizResponse>>(`/quizzes/${quizId}`);
    return unwrap(response);
  },

  listByUser: async (page = 0, size = 20): Promise<PageResponse<QuizResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<QuizResponse>>>('/quizzes', {
      params: { page, size },
    });
    return unwrap(response);
  },

  submit: async (quizId: number, request: QuizSubmitRequest): Promise<QuizSubmitResponse> => {
    const response = await apiClient.post<ApiResponse<QuizSubmitResponse>>(`/quizzes/${quizId}/submit`, request);
    return unwrap(response);
  },

  listAttempts: async (page = 0, size = 20): Promise<PageResponse<QuizAttemptResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<QuizAttemptResponse>>>('/quizzes/attempts', {
      params: { page, size },
    });
    return unwrap(response);
  },
};
