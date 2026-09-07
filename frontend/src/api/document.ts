import apiClient from './client';
import type { ApiResponse } from '../types/api';

export interface Document { 
  id: number; 
  title: string | null; 
  fileName: string; 
  fileSize: number; 
  mimeType: string; 
  sourceUrl: string | null; 
  pageCount: number | null; 
  status: string; 
  errorMessage: string | null; 
  createdBy: number | string; 
  createdAt: string; 
  updatedAt: string; 
}
export type DocumentResponse = Document;

export interface DocumentChunk { 
  id: number; 
  chunkIndex: number; 
  content: string; 
  charCount: number; 
  pageNumber: number | null; 
  heading: string | null; 
}
export interface DocumentListParams { 
  page?: number; 
  size?: number; 
  sort?: string | string[]; 
}
export interface DocumentPage { 
  content: Document[]; 
  page: number; 
  size: number; 
  totalElements: number; 
  totalPages: number; 
  last: boolean; 
  first: boolean; 
}
export interface DocumentUploadRequest { 
  file: File; 
  title?: string; 
}
export interface DocumentImportRequest { 
  url: string; 
  title?: string; 
}

const unwrap = <T>(r: { data: ApiResponse<T> }) => r.data.data;
export const documentApi = {
  upload: async ({ file, title }: DocumentUploadRequest) => { 
    const form = new FormData(); 
    form.append('file', file); 
    if (title) form.append('title', title); 
    return unwrap(await apiClient.post<ApiResponse<Document>>('/documents', form)); 
  },
  importUrl: async ({ url, title }: DocumentImportRequest) => 
    unwrap(await apiClient.post<ApiResponse<Document>>('/documents/import-url', null, { 
      params: { url, ...(title ? { title } : {}) } 
    })),
  list: async (params?: DocumentListParams) => 
    unwrap(await apiClient.get<ApiResponse<DocumentPage>>('/documents', { params })),
  get: async (id: number) => 
    unwrap(await apiClient.get<ApiResponse<Document>>(`/documents/${id}`)),
  chunks: async (id: number) => 
    unwrap(await apiClient.get<ApiResponse<DocumentChunk[]>>(`/documents/${id}/chunks`)),
  download: (id: number) => 
    apiClient.get(`/documents/${id}/download`, { responseType: 'blob' }),
  remove: async (id: number) => { 
    await apiClient.delete(`/documents/${id}`); 
  },
  adminRemove: async (id: number) => { 
    await apiClient.delete(`/documents/${id}/admin`); 
  },
};
