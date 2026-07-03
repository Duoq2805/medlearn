// Auth types
export interface User {
  id: string;
  email: string;
  name: string;
  role: 'student' | 'reviewer' | 'admin';
  createdAt: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}

// Disease types
export interface Disease {
  id: string;
  name: string;
  category: string;
  definition: string;
  symptoms: string[];
  causes: string[];
  diagnosis: string;
  treatment: string;
  prevention: string;
  icdCode?: string;
  createdAt: string;
  updatedAt: string;
}

// Symptom types
export interface Symptom {
  id: string;
  name: string;
  category: string;
  description?: string;
}

export interface SymptomSuggestion {
  disease: Disease;
  matchScore: number;
}

// Case Study types
export interface CaseStudy {
  id: string;
  title: string;
  description: string;
  patientAge: number;
  patientGender: string;
  presentingComplaint: string;
  diagnosis: string;
  lessonPoints: string[];
  keyFindings: string[];
  treatment: string;
  createdAt: string;
}

// Pagination
export interface PaginatedResult<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
}

// API Error
export interface ApiError {
  message: string;
  code?: string;
  details?: Record<string, unknown>;
}
