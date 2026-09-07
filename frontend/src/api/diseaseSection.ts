import apiClient from './client';
import type { 
  SectionTypeResponse, 
  SectionTemplateResponse,
  DiseaseSectionResponse,
  CreateDiseaseSectionRequest,
  UpdateDiseaseSectionRequest,
  SectionOrderRequest
} from '../types/diseaseSection';
import type { PageResponse } from '../types/api';

const unwrap = <T>(r: { data: { success: boolean; message?: string; data: T; timestamp?: string } }) => r.data.data;
export const diseaseSectionApi = {
  getAllSectionTypes: async (): Promise<SectionTypeResponse[]> => {
    const response = await apiClient.get('/sections/types');
    return unwrap(response);
  },
  
  getSectionTemplates: async (): Promise<SectionTemplateResponse[]> => {
    const response = await apiClient.get('/sections/templates');
    return unwrap(response);
  },
  
  getSectionsByVersion: async (versionId: number): Promise<DiseaseSectionResponse[]> => {
    const response = await apiClient.get(`/sections/version/${versionId}`);
    return unwrap(response);
  },
  
  getSectionsByVersionAndType: async (versionId: number, sectionType: string): Promise<DiseaseSectionResponse> => {
    const response = await apiClient.get(`/sections/version/${versionId}/type/${sectionType}`);
    return unwrap(response);
  },
  
  getSectionById: async (sectionId: number): Promise<DiseaseSectionResponse> => {
    const response = await apiClient.get(`/sections/${sectionId}`);
    return unwrap(response);
  },
  
  getSectionMarkdown: async (sectionId: number): Promise<string> => {
    const response = await apiClient.get(`/sections/${sectionId}/markdown`);
    return unwrap(response);
  },
  
  createSection: async (versionId: number, sectionData: CreateDiseaseSectionRequest): Promise<DiseaseSectionResponse> => {
    const response = await apiClient.post(`/sections`, sectionData, {
      params: { versionId },
    });
    return unwrap(response);
  },
  
  createSectionsBatch: async (versionId: number, sectionsData: CreateDiseaseSectionRequest[]): Promise<DiseaseSectionResponse[]> => {
    const response = await apiClient.post(`/sections/batch`, sectionsData, {
      params: { versionId },
    });
    return unwrap(response);
  },
  
  updateSection: async (sectionId: number, sectionData: UpdateDiseaseSectionRequest): Promise<DiseaseSectionResponse> => {
    const response = await apiClient.put(`/sections/${sectionId}`, sectionData);
    return unwrap(response);
  },
  
  deleteSection: async (sectionId: number): Promise<void> => {
    await apiClient.delete(`/sections/${sectionId}`);
    return;
  },
  
  deleteSectionsByVersion: async (versionId: number): Promise<void> => {
    await apiClient.delete(`/sections/version/${versionId}`);
    return;
  },
  
  reorderSections: async (versionId: number, orders: SectionOrderRequest[]): Promise<void> => {
    await apiClient.post(`/sections/version/${versionId}/reorder`, orders);
    return;
  },
  
  validateSections: async (versionId: number): Promise<void> => {
    await apiClient.post(`/sections/version/${versionId}/validate`);
    return;
  },
};
