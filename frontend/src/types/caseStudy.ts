export type CaseDifficulty = 'EASY' | 'MEDIUM' | 'HARD' | 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export interface CreateCaseStudyRequest {
  title: string;
  description?: string;
  difficulty?: CaseDifficulty;
  diagnosis?: string;
  learningNotes?: string;
  patientAge?: number;
  patientGender?: string;
  chiefComplaint?: string;
  caseQuestion?: string;
  explanation?: string;
  symptomIds?: number[];
}

export interface DiagnoseRequest {
  diagnosis: string;
}

export interface DiagnoseResponse {
  correct: boolean;
  feedback: string;
}

export interface CaseStudyDetailResponse {
  id: number;
  title: string;
  slug: string;
  description?: string;
  difficulty?: CaseDifficulty;
  diagnosis?: string;
  learningNotes?: string;
  patientAge?: number;
  patientGender?: string;
  chiefComplaint?: string;
  caseQuestion?: string;
  explanation?: string;
  viewCount?: number;
  isFeatured?: boolean;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
  symptomIds?: number[];
}

export interface CaseStudySummaryProjection {
  id: number;
  title: string;
  slug: string;
  updatedAt?: string;
}
