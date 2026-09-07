// ============================================
// DISEASE DRAFT - Backend DTOs & Frontend Types
// ============================================

export type DraftStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'ARCHIVED';
export type DraftMethod = 'MANUAL' | 'AI_FULL' | 'AI_ASSIST' | 'IMPORT';
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

export interface CreateDraftSectionContent {
  sectionType: DraftSectionType;
  title: string;
  content: string;
  orderIndex?: number;
}

export interface CreateDraftRequest {
  diseaseId?: number;
  title: string;
  sourceMethod?: DraftMethod;
  sourceDocumentId?: number;
  sections: CreateDraftSectionContent[];
}

export interface UpdateDraftSectionContent {
  id?: number;
  sectionType: DraftSectionType;
  title: string;
  content: string;
  orderIndex?: number;
}

export interface UpdateDraftRequest {
  title?: string;
  sections: UpdateDraftSectionContent[];
}

export interface DraftReviewRequest {
  action: string;
  note?: string;
}

export interface DiseaseDraftSectionResponse {
  id: number;
  sectionType: DraftSectionType;
  title: string;
  content: string;
  orderIndex?: number;
  wordCount?: number;
  aiGenerated?: boolean;
}

export interface DiseaseDraftResponse {
  id: number;
  diseaseId?: number;
  diseaseName?: string;
  title: string;
  status: DraftStatus;
  sourceMethod?: DraftMethod;
  sourceDocumentId?: number;
  aiModel?: string;
  aiTotalTokens?: number;
  aiLatencyMs?: number;
  reviewNote?: string;
  createdBy?: number;
  reviewedBy?: number;
  reviewedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  sections?: DiseaseDraftSectionResponse[];
}

/** Single source reference for a generated section */
export interface SectionSource {
  document: string;
  page: number | null;
  paragraph: number | null;
  confidence: number | null;
}

/** A single generated section with source traceability */
export interface GeneratedSection {
  content: string;
  sources: SectionSource[];
}

/** Optional provenance metadata from generation */
export interface DraftProvenance {
  generatedBy?: string;
  model?: string;
  generatedAt?: string;
  promptVersion?: string;
}

/** Full structured response from AI draft generation */
export interface DraftGenerationResponse {
  definition: GeneratedSection;
  etiology: GeneratedSection;
  symptoms: GeneratedSection;
  diagnosis: GeneratedSection;
  treatment: GeneratedSection;
  complications: GeneratedSection;
  prevention: GeneratedSection;
  references: GeneratedSection;
  provenance?: DraftProvenance;
}

/** Document upload success payload */
export interface DocumentUploadResponse {
  id: string;
  filename: string;
  url: string;
  size: number;
  contentType: string;
}

/** URL import success payload */
export interface UrlImportResponse {
  title: string;
  content: string;
  source: string;
}

/** Text extraction result from uploaded document */
export interface ExtractedTextResponse {
  documentId: string;
  filename: string;
  text: string;
  pageCount: number | null;
}

export type DraftCreationMethod =
  | 'manual'
  | 'upload'
  | 'import-url'
  | 'ai-generate';

/** Metadata about the draft's origin */
export interface DraftCreationMetadata {
  method: DraftCreationMethod;
  sourceLabel: string;
  sourceUrl?: string;
  originalFilename?: string;
}

export const DRAFT_SECTIONS: { label: string; key: string }[] = [
  { label: 'Definition', key: 'definition' },
  { label: 'Etiology', key: 'etiology' },
  { label: 'Symptoms', key: 'symptoms' },
  { label: 'Diagnosis', key: 'diagnosis' },
  { label: 'Treatment', key: 'treatment' },
  { label: 'Complications', key: 'complications' },
  { label: 'Prevention', key: 'prevention' },
  { label: 'References', key: 'references' },
];
