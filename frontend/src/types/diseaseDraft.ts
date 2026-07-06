// ============================================
// DISEASE DRAFT - Future-ready types
// Designed for source-grounded AI draft generation
// Backend-dependent fields marked with TBD
// ============================================

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

// ============================================
// Draft creation method enum
// ============================================

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

// ============================================
// Section definitions (matches existing editor)
// ============================================

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
