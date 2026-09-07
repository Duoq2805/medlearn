import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  FlashcardGenerateRequest,
  FlashcardGenerateResponse,
  FlashcardResponse,
  FlashcardDeckResponse,
  FlashcardUpdateRequest,
  FlashcardBatchUpdateRequest,
  FlashcardExportResponse,
  FlashcardStatsResponse,
  FlashcardProgressResponse,
  FlashcardReviewRequest,
  FlashcardReviewResponse,
} from '../types/flashcard';

const unwrap = <T>(r: { data: ApiResponse<T> }): T => r.data.data;

export const flashcardApi = {
  generate: async (request: FlashcardGenerateRequest): Promise<FlashcardGenerateResponse> => {
    const response = await apiClient.post<ApiResponse<FlashcardGenerateResponse>>('/flashcards/generate', request);
    return unwrap(response);
  },

  getById: async (id: number): Promise<FlashcardResponse> => {
    const response = await apiClient.get<ApiResponse<FlashcardResponse>>(`/flashcards/${id}`);
    return unwrap(response);
  },

  listByUser: async (page = 0, size = 20): Promise<PageResponse<FlashcardResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<FlashcardResponse>>>('/flashcards', {
      params: { page, size },
    });
    return unwrap(response);
  },

  listDecks: async (admin = false, page = 0, size = 20): Promise<PageResponse<FlashcardDeckResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<FlashcardDeckResponse>>>('/flashcards/decks', {
      params: { admin, page, size },
    });
    return unwrap(response);
  },

  listByDeck: async (deckId: number, page = 0, size = 20): Promise<PageResponse<FlashcardResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<FlashcardResponse>>>(`/flashcards/decks/${deckId}/cards`, {
      params: { page, size },
    });
    return unwrap(response);
  },

  update: async (id: number, request: FlashcardUpdateRequest): Promise<FlashcardResponse> => {
    const response = await apiClient.put<ApiResponse<FlashcardResponse>>(`/flashcards/${id}`, request);
    return unwrap(response);
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/flashcards/${id}`);
  },

  batchUpdate: async (request: FlashcardBatchUpdateRequest): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/flashcards/batch-update', request);
  },

  exportDeck: async (deckId: number, format = 'json'): Promise<FlashcardExportResponse> => {
    const response = await apiClient.get<ApiResponse<FlashcardExportResponse>>(`/flashcards/export/${deckId}`, {
      params: { format },
    });
    return unwrap(response);
  },

  getStats: async (): Promise<FlashcardStatsResponse> => {
    const response = await apiClient.get<ApiResponse<FlashcardStatsResponse>>('/flashcards/stats');
    return unwrap(response);
  },

  getDueCards: async (limit = 20): Promise<FlashcardProgressResponse[]> => {
    const response = await apiClient.get<ApiResponse<FlashcardProgressResponse[]>>('/flashcards/due', {
      params: { limit },
    });
    return unwrap(response);
  },

  review: async (id: number, request: FlashcardReviewRequest): Promise<FlashcardReviewResponse> => {
    const response = await apiClient.post<ApiResponse<FlashcardReviewResponse>>(`/flashcards/${id}/review`, request);
    return unwrap(response);
  },
};
