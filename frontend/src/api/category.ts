import apiClient from './client';
import type { ApiResponse } from '../types/api';
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../types/category';
export const categoryApi = {
  list: async (): Promise<Category[]> => (await apiClient.get<ApiResponse<Category[]>>('/categories')).data.data,
  getById: async (id: number): Promise<Category> => (await apiClient.get<ApiResponse<Category>>(`/categories/${id}`)).data.data,
  getBySlug: async (slug: string): Promise<Category> => (await apiClient.get<ApiResponse<Category>>(`/categories/slug/${encodeURIComponent(slug)}`)).data.data,
  create: async (request: CreateCategoryRequest): Promise<Category> => (await apiClient.post<ApiResponse<Category>>('/categories', request)).data.data,
  update: async (id: number, request: UpdateCategoryRequest): Promise<Category> => (await apiClient.put<ApiResponse<Category>>(`/categories/${id}`, request)).data.data,
  remove: async (id: number): Promise<void> => { await apiClient.delete<ApiResponse<null>>(`/categories/${id}`); },
};