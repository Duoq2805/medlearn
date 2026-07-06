// ============================================
// DISEASE DRAFT API STUBS
// TODO: These are placeholders for backend endpoints.
// Do not use in production until backend is implemented.
// ============================================

import apiClient from './client';
import type {
  DocumentUploadResponse,
  UrlImportResponse,
  ExtractedTextResponse,
  DraftGenerationResponse,
} from '../types/diseaseDraft';

// ============================================
// DOCUMENT UPLOAD
// TODO: POST /api/documents/upload - Not implemented
// Backend should: accept multipart, validate type, store locally,
// extract text, return ExtractedTextResponse
// ============================================
export const uploadDocument = async (
  _file: File
): Promise<DocumentUploadResponse> => {
  // 🔄 TODO: Replace with real API call when backend endpoint is ready
  // const formData = new FormData();
  // formData.append('file', file);
  // const response = await apiClient.post('/documents/upload', formData, {
  //   headers: { 'Content-Type': 'multipart/form-data' },
  // });
  // return response.data.data;
  throw new Error(
    'TODO: POST /api/documents/upload not implemented on backend'
  );
};

// ============================================
// DOCUMENT TEXT EXTRACTION
// TODO: POST /api/documents/{documentId}/extract - Not implemented
// Backend should: extract text from stored document, return ExtractedTextResponse
// ============================================
export const extractDocumentText = async (
  _documentId: string
): Promise<ExtractedTextResponse> => {
  throw new Error(
    'TODO: POST /api/documents/{documentId}/extract not implemented on backend'
  );
};

// ============================================
// URL IMPORT
// TODO: POST /api/documents/import-url - Not implemented
// Frontend sends URL, backend fetches + extracts article content
// ============================================
export const importUrl = async (
  _url: string
): Promise<UrlImportResponse> => {
  throw new Error(
    'TODO: POST /api/documents/import-url not implemented on backend'
  );
};

// ============================================
// AI DRAFT GENERATION
// TODO: POST /api/ai/draft/generate - Not implemented
// Backend should: receive extracted text or document IDs,
// generate structured draft with sources, return DraftGenerationResponse
// ============================================
export const generateAiDraft = async (
  _payload: {
    documentIds?: string[];
    extractedText?: string;
    importedUrl?: string;
    diseaseName?: string;
  }
): Promise<DraftGenerationResponse> => {
  throw new Error(
    'TODO: POST /api/ai/draft/generate not implemented on backend'
  );
};

// ============================================
// DRAFT CREATION (reuses existing endpoints)
// These use existing backend APIs
// ============================================

import { diseaseApi } from './disease';

/** Create a new disease draft (uses existing POST /api/diseases/draft) */
export const createDiseaseDraft = diseaseApi.createDisease;
