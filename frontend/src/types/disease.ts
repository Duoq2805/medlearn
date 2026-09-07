export type DiseaseStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'ARCHIVED';

export interface CreateDiseaseRequest {
  name: string;
  slug: string;
  categoryId?: number;
  initialVersionTitle?: string;
}

export interface CreateDiseaseDraftSection {
  sectionTypeId?: number;
  title: string;
  content: string;
  orderIndex?: number;
}

export interface CreateDiseaseDraftRequest {
  name: string;
  slug: string;
  categoryId?: number;
  sections?: CreateDiseaseDraftSection[];
}

export interface UpdateDiseaseRequest {
  name?: string;
  slug?: string;
  categoryId?: number;
}

export interface DiseaseSearchRequest {
  keyword?: string;
  categoryId?: number;
  symptomIds?: number[];
}

export interface DiseaseResponse {
  id: number;
  name: string;
  slug: string;
  categoryId?: number;
  categoryName?: string;
  currentVersionId?: number;
  status?: DiseaseStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface DiseaseDetailResponse {
  disease: DiseaseResponse;
  currentVersion?: any;
  sections?: any[];
}

export interface DiseaseSummaryProjection {
  id: number;
  name: string;
  slug: string;
  updatedAt?: string;
}
