export interface SymptomResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateSymptomRequest {
  name: string;
  description?: string;
}

export interface UpdateSymptomRequest {
  name?: string;
  description?: string;
}

export interface SymptomCheckerRequest {
  symptomIds: number[];
  limit?: number;
}

export interface SymptomMatchResult {
  diseaseId: number;
  diseaseName: string;
  slug: string;
  matchedCount: number;
  score: number;
  matchedSymptoms: string[];
  shortDescription?: string;
}

export interface SymptomAnalysisResponse {
  diseaseId: number;
  diseaseName: string;
  slug: string;
  score: number;
  matchedCount: number;
  totalSymptoms: number;
  matchedSymptoms: string[];
  missingSymptoms: string[];
}

export type SymptomSeverity = 'EMERGENCY' | 'URGENT' | 'MODERATE' | 'NORMAL';

export interface SymptomInfo {
  id: number;
  name: string;
  description?: string;
}

export interface SymptomAnalysisV2Response {
  diseaseId: number;
  diseaseName: string;
  slug: string;
  icdCode?: string;
  confidence: number;
  score: number;
  matchScore: number;
  severity: SymptomSeverity;
  severityExplanation?: string;
  clinicalExplanation?: string;
  recommendation?: string;
  matchedSymptoms: SymptomInfo[];
  missingSymptoms: SymptomInfo[];
  recommendedNextSymptoms: SymptomInfo[];
  matchedCriticalSymptoms: SymptomInfo[];
  missingCriticalSymptoms: SymptomInfo[];
  redFlags: string[];
  matchedCount: number;
  totalSymptoms: number;
}
