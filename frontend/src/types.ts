export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  fullName: string;
  role: string;
  roles: string[];
  isVerified: boolean;
  status: string;
  createdAt: string;
}

export interface AuthResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Disease {
  id: string;
  name: string;
  slug: string;
  category: string;
  icdCode?: string;
  definition: string;
  symptoms: string[];
  causes: string[];
  diagnosis: string;
  treatment: string;
  prevention: string;
  updatedAt: string;
}

export interface PaginatedResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last?: boolean;
}

export interface Symptom {
  id: string;
  name: string;
  slug: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface SymptomSuggestion {
  disease: {
    id: number;
    name: string;
    slug: string;
    definition: string;
    category: string;
  };
  diseaseId: number;
  diseaseName: string;
  diseaseSlug: string;
  matchCount: number;
  matchScore: number;
  matchedSymptoms: string[];
  shortDescription: string;
}

export interface CaseStudy {
  id: string;
  title: string;
  slug: string;
  description: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  isFeatured: boolean;
  viewCount: number;
  diagnosis: string;
  learningNotes: string;
  patientAge: number;
  patientGender: string;
  chiefComplaint: string;
  caseQuestion: string;
  explanation: string;
  symptomIds: number[];
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}
