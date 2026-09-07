export type DraftSectionType =
  | 'OVERVIEW'
  | 'DEFINITION'
  | 'CAUSES'
  | 'SYMPTOMS'
  | 'DIAGNOSIS'
  | 'TREATMENT'
  | 'PROGNOSIS'
  | 'COMPLICATIONS'
  | 'PREVENTION'
  | 'EPIDEMIOLOGY'
  | 'PATHOPHYSIOLOGY'
  | 'RISK_FACTORS'
  | 'CLINICAL_FEATURES'
  | 'INVESTIGATIONS'
  | 'MANAGEMENT'
  | 'DIFFERENTIAL_DIAGNOSIS'
  | 'REFERENCE';

export interface AiDraftRequest {
  diseaseId?: number;
  documentId?: number;
  title: string;
  sections: DraftSectionType[];
  model?: string;
  temperature?: number;
}

export type SummaryType = 'STUDENT' | 'CLINICAL' | 'EXAM' | 'QUICK_REVISION';

export interface SummaryRequest {
  summaryType: SummaryType;
  model?: string;
  temperature?: number;
}

export interface SummaryResponse {
  id: number;
  diseaseId: number;
  diseaseName: string;
  diseaseVersionId: number;
  summaryType: SummaryType;
  version: number;
  content: string;
  model: string;
  provider: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  latencyMs: number;
  createdAt: string;
}
