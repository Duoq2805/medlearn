export interface PagedResponse<T> {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}

export interface DiseaseVersionResponse {
  id: number;
  diseaseId: number;
  versionNumber: number;
  status: string;
  moderationNote: string | null;
  createdById: number;
  reviewedById: number | null;
  reviewedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDiseaseVersionRequest {
  title: string;
  note: string | null;
}

export interface UpdateDiseaseVersionRequest {
  moderationNote: string | null;
}

export interface ModerationRequest {
  note: string | null;
}

export interface VersionStatus {
  DRAFT: string;
  PENDING_REVIEW: string;
  APPROVED: string;
  ARCHIVED: string;
}
// We'll use string literals for status instead of enum for simplicity
